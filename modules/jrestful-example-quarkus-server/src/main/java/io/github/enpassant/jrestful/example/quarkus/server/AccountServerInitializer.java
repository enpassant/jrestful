package io.github.enpassant.jrestful.example.quarkus.server;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jrestful.server.vertx.VertxRestServer;

@ApplicationScoped
public class AccountServerInitializer {
  void initialize(@Observes final Router router, final AccountManager accounts) {
    router.route().handler(BodyHandler.create());
    new AccountRestServer(new VertxRestServer(router), accounts);
  }
}
