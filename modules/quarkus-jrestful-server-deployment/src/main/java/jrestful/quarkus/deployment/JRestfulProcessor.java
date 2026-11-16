package jrestful.quarkus.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.builder.item.SimpleBuildItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jrestful.Root;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.fp.None;
import jrestful.fp.Some;
import jrestful.link.RelLink;
import jrestful.quarkus.RestApiProvider;
import jrestful.quarkus.runtime.JRestfulBootstrap;
import jrestful.quarkus.runtime.JRestfulJacksonConfiguration;
import jrestful.quarkus.runtime.JRestfulRecorder;
import jrestful.quarkus.runtime.JRestfulResourceRegistry;
import jrestful.type.MediaType;
import jrestful.type.TypeList;
import jrestful.type.TypeObject;
import jrestful.type.Types;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTransformation;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

final class JRestfulProcessor {
  private static final String FEATURE = "jrestful-server";
  private static final DotName JREST_TRANSITION = DotName.createSimple(
    "jrestful.server.jaxrs.JRestTransition");
  private static final DotName JREST_TRANSITIONS = DotName.createSimple(
    "jrestful.server.jaxrs.JRestTransitions");
  private static final DotName PATH = DotName.createSimple("jakarta.ws.rs.Path");
  private static final DotName GET = DotName.createSimple("jakarta.ws.rs.GET");
  private static final DotName PUT = DotName.createSimple("jakarta.ws.rs.PUT");
  private static final DotName DELETE = DotName.createSimple("jakarta.ws.rs.DELETE");
  private static final DotName HEAD = DotName.createSimple("jakarta.ws.rs.HEAD");
  private static final DotName REQUEST_CONTEXT = DotName.createSimple(
    "jrestful.server.RequestContext");
  private static final DotName API_PROVIDER = DotName.createSimple(RestApiProvider.class);
  private static final Set<DotName> CDI_SECURITY_ANNOTATIONS = Set.of(
    DotName.createSimple("jakarta.annotation.security.DenyAll"),
    DotName.createSimple("jakarta.annotation.security.PermitAll"),
    DotName.createSimple("jakarta.annotation.security.RolesAllowed")
  );

  @BuildStep
  FeatureBuildItem feature(final Capabilities capabilities) {
    if (capabilities.isPresent(Capability.REST)
      || capabilities.isPresent(Capability.RESTEASY_REACTIVE)
      || capabilities.isPresent(Capability.RESTEASY)) {
      throw new IllegalStateException(
        "quarkus-jrestful-server and Quarkus REST are mutually exclusive; remove the Quarkus REST extension");
    }
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  AnnotationsTransformerBuildItem disableCdiSecurityInterceptors() {
    return new AnnotationsTransformerBuildItem(AnnotationTransformation.forMethods()
      .whenAnyMatch(JREST_TRANSITION, JREST_TRANSITIONS)
      .transform(context -> context.remove(
        annotation -> CDI_SECURITY_ANNOTATIONS.contains(annotation.name()))));
  }

  @BuildStep
  ResourceModel resources(
    final CombinedIndexBuildItem combinedIndex,
    final BuildProducer<AdditionalBeanBuildItem> additionalBeans,
    final BuildProducer<ReflectiveClassBuildItem> reflection
  ) {
    final IndexView index = combinedIndex.getIndex();
    final Set<MethodInfo> methods = transitionMethods(index);
    final Set<String> resourceNames = new LinkedHashSet<>();

    for (final MethodInfo method : methods) {
      validateMethod(method);
      final ClassInfo resource = method.declaringClass();
      if (!resource.hasAnnotation(PATH)) {
        throw new IllegalStateException(
          "jrestful resource must declare @Path: " + resource.name());
      }
      resourceNames.add(resource.name().toString());
    }
    if (resourceNames.isEmpty()) {
      throw new IllegalStateException("No @Path resource with @JRestTransition methods was found");
    }

    final List<ClassInfo> providers = index.getAllKnownImplementations(API_PROVIDER).stream()
      .filter(provider -> !provider.isAbstract())
      .toList();
    if (providers.size() != 1) {
      throw new IllegalStateException(
        "Exactly one RestApiProvider implementation is required, found: " + providers.size());
    }

    additionalBeans.produce(AdditionalBeanBuildItem.builder()
      .addBeanClasses(resourceNames)
      .addBeanClass(providers.getFirst().name().toString())
      .setDefaultScope(DotName.createSimple(ApplicationScoped.class))
      .setUnremovable()
      .build());
    additionalBeans.produce(AdditionalBeanBuildItem.builder()
      .addBeanClasses(JRestfulBootstrap.class, JRestfulJacksonConfiguration.class)
      .setUnremovable()
      .build());

    reflection.produce(ReflectiveClassBuildItem.builder(resourceNames)
      .methods()
      .reason("jrestful invokes annotated resource methods")
      .build());
    reflection.produce(ReflectiveClassBuildItem.builder(
        RestApi.class, Root.class, Transition.class, RelLink.class,
        None.class, Some.class, Types.class, MediaType.class, TypeList.class, TypeObject.class)
      .methods().fields().reason("jrestful API JSON model").build());

    return new ResourceModel(List.copyOf(resourceNames));
  }

  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  SyntheticBeanBuildItem registry(final ResourceModel model, final JRestfulRecorder recorder) {
    return SyntheticBeanBuildItem.configure(JRestfulResourceRegistry.class)
      .scope(Singleton.class)
      .runtimeValue(recorder.registry(model.resourceClassNames()))
      .unremovable()
      .done();
  }

  private Set<MethodInfo> transitionMethods(final IndexView index) {
    final Set<MethodInfo> methods = new LinkedHashSet<>();
    collectMethods(index.getAnnotations(JREST_TRANSITION), methods);
    collectMethods(index.getAnnotations(JREST_TRANSITIONS), methods);
    return methods;
  }

  private void collectMethods(
    final Collection<AnnotationInstance> annotations,
    final Set<MethodInfo> methods
  ) {
    for (final AnnotationInstance annotation : annotations) {
      if (annotation.target().kind() == AnnotationTarget.Kind.METHOD) {
        methods.add(annotation.target().asMethod());
      }
    }
  }

  private void validateMethod(final MethodInfo method) {
    if (method.returnType().kind() != Type.Kind.VOID
      || method.parametersCount() != 1
      || !method.parameterType(0).name().equals(REQUEST_CONTEXT)) {
      throw new IllegalStateException(
        "jrestful resource method must return void and accept one RequestContext: " + method);
    }
    if (!method.hasAnnotation(GET) && !method.hasAnnotation(PUT)
      && !method.hasAnnotation(DELETE) && !method.hasAnnotation(HEAD)) {
      throw new IllegalStateException(
        "jrestful resource method requires @GET, @PUT, @DELETE or @HEAD: " + method);
    }
  }

  static final class ResourceModel extends SimpleBuildItem {
    private final List<String> resourceClassNames;

    ResourceModel(final List<String> resourceClassNames) {
      this.resourceClassNames = resourceClassNames;
    }

    List<String> resourceClassNames() {
      return resourceClassNames;
    }
  }
}
