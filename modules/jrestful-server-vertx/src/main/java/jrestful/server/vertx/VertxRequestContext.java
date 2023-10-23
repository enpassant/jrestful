package jrestful.server.vertx;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import jrestful.server.RequestContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class VertxRequestContext extends RequestContext {
  private final RoutingContext context;

  public VertxRequestContext(final RoutingContext context) {
    this.context = context;
  }

  public <T> Optional<T> parseBodyAs(final Class<T> clazz) {
    try {
      final JsonObject jsonObject = context.body().asJsonObject();
      return Optional.of(jsonObject.mapTo(clazz));
    } catch (final Exception e) {
      final HttpServerResponse response = context.response();
      response.setStatusCode(400).end("Malformed syntax: " + clazz.getSimpleName());
      return Optional.empty();
    }
  }

  public <T> CompletionStage<Void> json(final T object) {
    return context.json(object).toCompletionStage();
  }

  @Override
  public List<String> queryParams(final String key) {
    return context.queryParam(key);
  }

  @Override
  public CompletionStage<Void> sendTextWithCode(final int code, final String message) {
    final HttpServerResponse response = context.response();
    return response.setStatusCode(404).end(message).toCompletionStage();
  }

  @Override
  public void next() {
    context.next();
  }

  @Override
  public <T> CompletionStage<Void> json(final int code, final T object) {
    final HttpServerResponse response = context.response();
    response.setStatusCode(code);
    return context.json(object).toCompletionStage();
  }

  public User getUser() {
    return context.user();
  }
}
