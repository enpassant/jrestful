package io.github.enpassant.jrestful.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {
  private static final Logger LOGGER = Logger.getLogger(MainVerticle.class.getName());

  @Override
  public void start(final Promise<Void> startPromise) {
    final RestServerVerticle restServerVerticleOTP = new RestServerVerticle();
    final Future<String> futureOTP = vertx.deployVerticle(restServerVerticleOTP);

    final RestServerVerticle restServerVerticleCIB = new RestServerVerticle();
    final JsonObject configCIB = new JsonObject().put("port", 8100);
    final DeploymentOptions deploymentOptionsCIB = new DeploymentOptions().setConfig(configCIB);
    final Future<String> futureCIB = vertx.deployVerticle(restServerVerticleCIB, deploymentOptionsCIB);

    Future.all(futureOTP, futureCIB).compose(compositeFuture -> {
      final WebClientVerticle webClientVerticle = new WebClientVerticle();
      return vertx.deployVerticle(webClientVerticle);
    }).onComplete(result -> {
      startPromise.complete();
      if (!System.getProperty("shutdown.when.complete", "").isBlank()) {
        vertx.close();
      }
    });
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
