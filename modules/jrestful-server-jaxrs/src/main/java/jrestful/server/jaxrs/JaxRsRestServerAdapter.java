package jrestful.server.jaxrs;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PUT;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;
import jrestful.server.RestServer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Registers JAX-RS annotated resource methods in a jrestful {@link RestServer}. */
public final class JaxRsRestServerAdapter {
  private final RestServer server;
  private final Function<Set<String>, RestAuthorization> authorizationFactory;

  public JaxRsRestServerAdapter(final RestServer server) {
    this(server, roles -> {
      final var authorizations = roles.stream()
        .map(server::createPermissionBased)
        .toList();
      return context -> authorizations.stream().anyMatch(authorization -> authorization.match(context));
    });
  }

  public JaxRsRestServerAdapter(
    final RestServer server,
    final Function<Set<String>, RestAuthorization> authorizationFactory
  ) {
    this.server = server;
    this.authorizationFactory = authorizationFactory;
  }

  public void register(final RestApi api, final Object... resources) {
    if (resources.length == 0) {
      throw new IllegalArgumentException("At least one JAX-RS resource is required");
    }
    final Map<String, Consumer<RequestContext>> headProcessors = new HashMap<>();
    for (final Object resource : resources) {
      collectHeadProcessors(resource, headProcessors);
    }

    server.init(api, ignored -> Arrays.stream(resources).forEach(resource -> {
      final Class<?> resourceType = resourceType(resource);
      final String rootPath = pathOf(resourceType.getAnnotation(Path.class));
      Arrays.stream(resourceType.getDeclaredMethods())
        .filter(method -> !method.isAnnotationPresent(HEAD.class))
        .filter(method -> method.getAnnotationsByType(JRestTransition.class).length > 0)
        .sorted(Comparator.comparingInt(method -> transitionOrder(api, method)))
        .forEach(method -> registerMethod(api, resource, rootPath, headProcessors, method));
    }));
  }

  private int transitionOrder(final RestApi api, final Method method) {
    final JRestTransition[] bindings = method.getAnnotationsByType(JRestTransition.class);
    if (bindings.length != 1) return Integer.MAX_VALUE;

    final Transition[] transitions = api.transition();
    for (int index = 0; index < transitions.length; index++) {
      if (transitions[index].name().equals(bindings[0].value())) return index;
    }
    return Integer.MAX_VALUE;
  }

  private void registerMethod(
    final RestApi api,
    final Object resource,
    final String rootPath,
    final Map<String, Consumer<RequestContext>> headProcessors,
    final Method method
  ) {
    validateHandler(method);
    final JRestTransition[] bindings = method.getAnnotationsByType(JRestTransition.class);
    if (bindings.length != 1) {
      throw new IllegalArgumentException("Resource operation must declare exactly one @JRestTransition: " + method);
    }
    final JRestTransition binding = bindings[0];
    final Transition transition = api.getTransition(binding.value())
      .orElseThrow(() -> new IllegalArgumentException("Unknown jrestful transition: " + binding.value()));
    validateHttpMethod(method, transition);
    validateMediaTypes(method, transition);

    final String path = joinPaths(rootPath, pathOf(method.getAnnotation(Path.class)));
    final RestAuthorization authorization = authorization(resourceType(resource), method);
    final Consumer<RequestContext> process = context -> invoke(resource, method, context);
    final Consumer<RequestContext> processHead = headProcessors.get(binding.value());

    if (processHead == null) {
      server.buildHandler(binding.value(), path, authorization, process);
    } else {
      server.buildHandler(binding.value(), path, authorization, processHead, process);
    }
  }

  private void collectHeadProcessors(
    final Object resource,
    final Map<String, Consumer<RequestContext>> result
  ) {
    Arrays.stream(resourceType(resource).getDeclaredMethods())
      .filter(method -> method.isAnnotationPresent(HEAD.class))
      .forEach(method -> {
        validateHandler(method);
        final JRestTransition[] transitions = method.getAnnotationsByType(JRestTransition.class);
        if (transitions.length == 0) {
          throw new IllegalArgumentException("@HEAD method must declare at least one @JRestTransition: " + method);
        }
        for (final JRestTransition transition : transitions) {
          final Consumer<RequestContext> previous = result.put(
            transition.value(), context -> invoke(resource, method, context));
          if (previous != null) {
            throw new IllegalArgumentException("More than one @HEAD method for transition: " + transition.value());
          }
        }
      });
  }

  private Class<?> resourceType(final Object resource) {
    Class<?> type = resource.getClass();
    while (type != Object.class && !type.isAnnotationPresent(Path.class)) {
      type = type.getSuperclass();
    }
    if (type == Object.class) {
      throw new IllegalArgumentException("JAX-RS resource must declare @Path: " + resource.getClass());
    }
    return type;
  }

  private RestAuthorization authorization(final Class<?> resourceType, final Method method) {
    if (method.isAnnotationPresent(DenyAll.class)) return context -> false;
    if (method.isAnnotationPresent(PermitAll.class)) return context -> true;
    final RolesAllowed methodRoles = method.getAnnotation(RolesAllowed.class);
    if (methodRoles != null) return rolesAuthorization(methodRoles);

    if (resourceType.isAnnotationPresent(DenyAll.class)) return context -> false;
    if (resourceType.isAnnotationPresent(PermitAll.class)) return context -> true;
    final RolesAllowed typeRoles = resourceType.getAnnotation(RolesAllowed.class);
    return typeRoles == null ? context -> true : rolesAuthorization(typeRoles);
  }

  private RestAuthorization rolesAuthorization(final RolesAllowed rolesAllowed) {
    final Set<String> roles = Arrays.stream(rolesAllowed.value())
      .filter(role -> !role.isBlank())
      .collect(Collectors.toUnmodifiableSet());
    if (roles.isEmpty()) {
      throw new IllegalArgumentException("@RolesAllowed must declare at least one non-blank role");
    }
    return authorizationFactory.apply(roles);
  }

  private void validateHandler(final Method method) {
    if (method.getReturnType() != void.class
      || !Arrays.equals(method.getParameterTypes(), new Class<?>[]{RequestContext.class})) {
      throw new IllegalArgumentException(
        "Resource method must return void and accept exactly one RequestContext: " + method);
    }
    method.trySetAccessible();
  }

  private void validateHttpMethod(final Method method, final Transition transition) {
    final String annotatedMethod = httpMethod(method);
    if (annotatedMethod == null) {
      throw new IllegalArgumentException("Missing supported JAX-RS HTTP method annotation: " + method);
    }
    if (!transition.relLink().method().name().equals(annotatedMethod)) {
      throw new IllegalArgumentException("HTTP method " + annotatedMethod + " does not match transition "
        + transition.name() + " (" + transition.relLink().method() + ")");
    }
  }

  private void validateMediaTypes(final Method method, final Transition transition) {
    final Consumes consumes = method.getAnnotation(Consumes.class);
    if (consumes != null && transition.relLink().in().isPresent()
      && Arrays.stream(consumes.value()).noneMatch(
        transition.relLink().in().orElse("")::equalsIgnoreCase)) {
      throw new IllegalArgumentException("@Consumes does not match transition " + transition.name());
    }
    final Produces produces = method.getAnnotation(Produces.class);
    if (produces != null
      && Arrays.stream(produces.value()).noneMatch(transition.relLink().out()::equalsIgnoreCase)) {
      throw new IllegalArgumentException("@Produces does not match transition " + transition.name());
    }
  }

  private String httpMethod(final Method method) {
    if (method.isAnnotationPresent(GET.class)) return HttpMethod.GET;
    if (method.isAnnotationPresent(PUT.class)) return HttpMethod.PUT;
    if (method.isAnnotationPresent(DELETE.class)) return HttpMethod.DELETE;
    return Arrays.stream(method.getAnnotations())
      .map(Annotation::annotationType)
      .map(type -> type.getAnnotation(HttpMethod.class))
      .filter(Objects::nonNull)
      .map(HttpMethod::value)
      .findFirst()
      .orElse(null);
  }

  private void invoke(final Object resource, final Method method, final RequestContext context) {
    try {
      method.invoke(resource, context);
    } catch (final IllegalAccessException exception) {
      throw new IllegalStateException("Cannot access resource method: " + method, exception);
    } catch (final InvocationTargetException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof final RuntimeException runtimeException) throw runtimeException;
      if (cause instanceof final Error error) throw error;
      throw new IllegalStateException("Resource method failed: " + method, cause);
    }
  }

  private String pathOf(final Path path) {
    return path == null ? "" : path.value();
  }

  private String joinPaths(final String root, final String child) {
    final String combined = ("/" + root + "/" + child).replaceAll("/{2,}", "/");
    return combined.length() > 1 && combined.endsWith("/")
      ? combined.substring(0, combined.length() - 1)
      : combined;
  }
}
