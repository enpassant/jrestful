package io.github.enpassant.jrestful.example.starter;

import io.github.enpassant.jrestful.example.account.AccountManager;
import io.github.enpassant.jrestful.example.account.AccountRestServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {
  private static final Logger LOGGER = Logger.getLogger(MainVerticle.class.getName());

  @Override
  public void start(final Promise<Void> startPromise) {
    final HttpServer server = vertx.createHttpServer();
    final Router router = Router.router(vertx);

    final AccountManager accountManager = new AccountManager();
    final VertxAuthenticate vertxAuthenticate = new VertxAuthenticate(vertx);
    final JWTAuth jwtAuth = vertxAuthenticate.getJwtAuth();
    final ChainAuthHandler authHandler = ChainAuthHandler.any();

    authHandler.add(BasicAuthHandler.create(vertxAuthenticate));
    authHandler.add(JWTAuthHandler.create(jwtAuth));

    router.route("/auth/*").handler(authHandler);
    router.route("/auth/*").failureHandler(this::handleFailure);

    router.route().handler(BodyHandler.create());

    final AccountRestServer accountRestServer =
      new AccountRestServer(router, accountManager, vertxAuthenticate);

    server.requestHandler(router);
    server.listen(8000);

    final WebClientVerticle webClientVerticle = new WebClientVerticle();
    vertx.deployVerticle(webClientVerticle);
  }

  private void handleFailure(final RoutingContext routingContext) {
    final Throwable failure = routingContext.failure();
    if (failure instanceof final HttpException httpException) {
      LOGGER.info(() -> MessageFormat.format("Http failure: {0}", httpException.getMessage()));
      routingContext.response()
        .setStatusCode(httpException.getStatusCode())
        .end(httpException.getMessage());
    } else {
      LOGGER.severe(() -> MessageFormat.format("Failure: {0}", failure.getMessage()));
      throw new RuntimeException(failure);
    }
  }
}
