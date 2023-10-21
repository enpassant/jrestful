package jrestful.server.vertx;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.AuthorizationContext;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jrestful.Method;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.link.Link;
import jrestful.link.RelLink;
import jrestful.server.RestServer;
import jrestful.server.RestServerHandler;
import jrestful.type.MediaType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VertxRestServer implements RestServer<RoutingContext, Authorization> {

  protected RestApi restApi;
  protected final Router router;

  private final Map<String, List<Link>> mediaTypeLinks = new HashMap<>();

  public VertxRestServer(final Router router) {
    this.router = router;
  }

  public void init(
    final RestApi restApi,
    final Consumer<RestServer<RoutingContext, Authorization>> buildHandlers
  ) {
    this.restApi = restApi;

    buildHandler(API, "/", permissionAll(), this::handleApi);
    buildHandlers.accept(this);
  }

  private Authorization permissionAll() {
    return new Authorization() {
      @Override
      public boolean match(final AuthorizationContext context) {
        return true;
      }

      @Override
      public boolean verify(final Authorization authorization) {
        return true;
      }
    };
  }

  protected void handleApi(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    routingContext.json(restApi);
  }

  public void buildHandler(
    final String transitionName,
    final String uri,
    final Authorization authorization,
    final BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> process
  ) {
    restApi.getTransition(transitionName).ifPresent(
      transition -> createHandler(transition, uri, authorization)
        .setProcess(process)
    );
  }

  public void buildHandler(
    final String transitionName,
    final String uri,
    final Authorization authorization,
    final BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> processHead,
    final BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> process
  ) {
    restApi.getTransition(transitionName).ifPresent(
      transition -> createHandler(transition, uri, authorization)
        .setProcessHead(processHead)
        .setProcess(process)
    );
  }

  public <T> Optional<T> parseBodyAs(final RoutingContext routingContext, final Class<T> clazz) {
    try {
      final JsonObject jsonObject = routingContext.body().asJsonObject();
      return Optional.of(jsonObject.mapTo(clazz));
    } catch (final Exception e) {
      final HttpServerResponse response = routingContext.response();
      response.setStatusCode(400).end("Malformed syntax: " + clazz.getSimpleName());
      return Optional.empty();
    }
  }

  public RestServerHandler<RoutingContext> createHandler(
    final Transition transition,
    final String uri,
    final Authorization authorization
  ) {
    final RelLink relLink = transition.relLink();
    final String contextName = transition.context().name();
    final Method method = relLink.method();
    final HttpMethod httpMethod = convertMethod(method);
    final String contentType = relLink.out().name();
    final Link link = new Link(uri, relLink);
    final List<Link> links = mediaTypeLinks.getOrDefault(
      contextName,
      new ArrayList<>()
    );
    links.add(link);
    mediaTypeLinks.putIfAbsent(contextName, links);
    final VertxRestServerHandler handler = new VertxRestServerHandler(
      this,
      contentType,
      relLink.rel(),
      authorization
    );
    router.route(httpMethod, uri)
      .consumes(relLink.in().map(MediaType::name).orElse("*/*"))
      .produces(contentType)
      .handler(handler);

    router.route(HttpMethod.HEAD, uri)
      .consumes(relLink.in().map(MediaType::name).orElse("*/*"))
      .produces(contentType)
      .handler(handler);

    return handler;
  }

  public List<Link> getLinks(final String contentType) {
    return mediaTypeLinks.getOrDefault(
      contentType,
      List.of()
    );
  }

  private HttpMethod convertMethod(final Method method) {
    return switch (method) {
      case HEAD -> HttpMethod.HEAD;
      case GET -> HttpMethod.GET;
      case PUT -> HttpMethod.PUT;
      case DELETE -> HttpMethod.DELETE;
    };
  }
}
