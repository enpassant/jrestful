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
import jrestful.server.vertx.VertxRestServer;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class RestServerVerticle extends AbstractVerticle {
  private static final Logger LOGGER = Logger.getLogger(RestServerVerticle.class.getName());

  @Override
  public void start(final Promise<Void> startPromise) {
    final Integer port = config().getInteger("port", 8000);

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

    final VertxRestServer vertxRestServer = new VertxRestServer(router);
    final AccountRestServer accountRestServer =
      new AccountRestServer(vertxRestServer, accountManager);

    server.requestHandler(router);
    server.listen(port);

    startPromise.complete();
  }

  private void handleFailure(final RoutingContext routingContext) {
    final Throwable failure = routingContext.failure();
    if (failure instanceof final HttpException httpException) {
      final String message = MessageFormat.format(
        "{0} [{1}]",
        httpException.getMessage(),
        httpException.getPayload()
      );
      LOGGER.info(() -> message);
      routingContext.response().setStatusCode(httpException.getStatusCode()).end(message);
    } else {
      LOGGER.severe(() -> MessageFormat.format("Failure: {0}", failure.getMessage()));
      throw new RuntimeException(failure);
    }
  }
}
