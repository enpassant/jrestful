package io.github.enpassant.jrestful.example.quarkus.server;

import io.smallrye.jwt.build.Jwt;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jrestful.server.vertx.VertxRestServer;

import java.util.Set;

@ApplicationScoped
public class AccountServerInitializer {
  private static final String ISSUER = "jrestful-example";

  void initialize(@Observes final Router router, final AccountManager accounts) {
    router.route().handler(BodyHandler.create());
    router.route("/account*").handler(this::issueToken);

    final VertxRestServer server = new VertxRestServer(router);
    new AccountRestServer(server, accounts);
  }

  private void issueToken(final RoutingContext context) {
    if (context.user() != null) {
      final QuarkusHttpUser user = (QuarkusHttpUser) context.user();
      final String username = user.getSecurityIdentity().getPrincipal().getName();
      final Set<String> roles = user.getSecurityIdentity().getRoles();
      final String token = Jwt.issuer(ISSUER)
        .upn(username)
        .groups(roles)
        .sign();
      context.response().putHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
    context.next();
  }
}
