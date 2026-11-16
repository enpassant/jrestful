package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.smallrye.jwt.build.Jwt;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.Set;

@ApplicationScoped
public class AccountServerInitializer {
  private static final String ISSUER = "jrestful-example";

  void initialize(@Observes @Priority(100) final Router router) {
    router.route("/account*").handler(this::issueToken);
  }

  private void issueToken(final RoutingContext context) {
    if (context.user() != null) {
      final QuarkusHttpUser user = (QuarkusHttpUser) context.user();
      final String username = user.getSecurityIdentity().getPrincipal().getName();
      final Set<String> roles = user.getSecurityIdentity().getRoles();
      final String token = Jwt.issuer(ISSUER).upn(username).groups(roles).sign();
      context.response().putHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
    context.next();
  }
}
