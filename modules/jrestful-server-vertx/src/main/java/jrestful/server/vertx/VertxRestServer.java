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
import jrestful.server.RequestContext;
import jrestful.server.RestServer;
import jrestful.server.RestServerHandler;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VertxRestServer implements RestServer<RoutingContext, Authorization> {

  protected RestApi restApi;
  protected final Router router;

  private final Map<String, List<Link>> mediaTypeLinks = new HashMap<>();
  private Map<String, List<Transition>> mediaTypeTransitions = new HashMap<>();
  private final Map<String, String> transitionPaths = new HashMap<>();

  public VertxRestServer(final Router router) {
    this.router = router;
  }

  public void init(
    final RestApi restApi,
    final Consumer<RestServer<RoutingContext, Authorization>> buildHandlers
  ) {
    this.restApi = restApi;

    this.mediaTypeTransitions = Arrays.stream(restApi.transition())
      .collect(Collectors.groupingBy(transition -> transition.context().name()));

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

  protected void handleApi(final RestServerHandler<RoutingContext> restServerHandler, final RequestContext<RoutingContext> requestContext) {
    requestContext.getContext().json(restApi);
  }

  public void buildHandler(
    final String transitionName,
    final String path,
    final Authorization authorization,
    final BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> process
  ) {
    restApi.getTransition(transitionName).ifPresent(
      transition -> createHandler(transition, path, authorization)
        .setProcess(process)
    );
  }

  public void buildHandler(
    final String transitionName,
    final String path,
    final Authorization authorization,
    final BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> processHead,
    final BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> process
  ) {
    restApi.getTransition(transitionName).ifPresent(
      transition -> createHandler(transition, path, authorization)
        .setProcessHead(processHead)
        .setProcess(process)
    );
  }

  public <T> Optional<T> parseBodyAs(final RequestContext<RoutingContext> requestContext, final Class<T> clazz) {
    try {
      final JsonObject jsonObject = requestContext.getContext().body().asJsonObject();
      return Optional.of(jsonObject.mapTo(clazz));
    } catch (final Exception e) {
      final HttpServerResponse response = requestContext.getContext().response();
      response.setStatusCode(400).end("Malformed syntax: " + clazz.getSimpleName());
      return Optional.empty();
    }
  }

  public RestServerHandler<RoutingContext> createHandler(
    final Transition transition,
    final String path,
    final Authorization authorization
  ) {
    final RelLink relLink = transition.relLink();
    final String contextName = transition.context().name();
    final Method method = relLink.method();
    final HttpMethod httpMethod = convertMethod(method);
    final String contentType = relLink.out();
    final Link link = new Link(path, relLink);
    final List<Link> links = mediaTypeLinks.getOrDefault(
      contextName,
      new ArrayList<>()
    );
    links.add(link);
    transitionPaths.put(transition.name(), path);
    mediaTypeLinks.putIfAbsent(contextName, links);
    final VertxRestServerHandler handler = new VertxRestServerHandler(
      this,
      contentType,
      relLink.rel(),
      authorization
    );
    router.route(httpMethod, path)
      .consumes(relLink.in().orElse("*/*"))
      .produces(contentType)
      .handler(handler);

    router.route(HttpMethod.HEAD, path)
      .consumes(relLink.in().orElse("*/*"))
      .produces(contentType)
      .handler(handler);

    return handler;
  }

  public List<Link> getLinks(final String contentType) {
    return mediaTypeTransitions.getOrDefault(
        contentType,
        List.of()
      ).stream()
      .map(transition -> new Link(transitionPaths.get(transition.name()), transition.relLink()))
      .collect(Collectors.toList());
//    return mediaTypeLinks.getOrDefault(
//      contentType,
//      List.of()
//    );
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
