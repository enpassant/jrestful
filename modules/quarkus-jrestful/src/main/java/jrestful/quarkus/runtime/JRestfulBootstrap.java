package jrestful.quarkus.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jrestful.quarkus.RestApiProvider;
import jrestful.server.RestAuthorization;
import jrestful.server.jaxrs.JaxRsRestServerAdapter;
import jrestful.server.vertx.VertxRequestContext;
import jrestful.server.vertx.VertxRestServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class JRestfulBootstrap {
  @Inject
  JRestfulResourceRegistry registry;

  @Inject
  Instance<RestApiProvider> apiProviders;

  void initialize(@Observes @Priority(200) final Router router) {
    if (!apiProviders.isResolvable()) {
      throw new IllegalStateException("Exactly one RestApiProvider CDI bean is required");
    }

    router.route().handler(BodyHandler.create());
    final VertxRestServer server = new VertxRestServer(router);
    final JaxRsRestServerAdapter adapter = new JaxRsRestServerAdapter(server, this::roles);
    adapter.register(apiProviders.get().create(server), resources());
  }

  private Object[] resources() {
    final List<Object> resources = new ArrayList<>();
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    for (final String className : registry.resourceClassNames()) {
      try {
        final Class<?> resourceClass = Class.forName(className, false, classLoader);
        final InstanceHandle<?> handle = Arc.container().instance(resourceClass);
        if (!handle.isAvailable()) {
          throw new IllegalStateException("jrestful resource is not a CDI bean: " + className);
        }
        resources.add(handle.get());
      } catch (final ClassNotFoundException exception) {
        throw new IllegalStateException("Cannot load jrestful resource: " + className, exception);
      }
    }
    return resources.toArray();
  }

  private RestAuthorization roles(final Set<String> roles) {
    return context -> context instanceof final VertxRequestContext vertxContext
      && vertxContext.getUser() instanceof final QuarkusHttpUser user
      && roles.stream().anyMatch(user.getSecurityIdentity()::hasRole);
  }
}
