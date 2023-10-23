package jrestful.server.vertx;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jrestful.Method;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.link.Link;
import jrestful.link.RelLink;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;
import jrestful.server.RestServer;
import jrestful.server.RestServerHandler;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VertxRestServer implements RestServer {

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
    final Consumer<RestServer> buildHandlers
  ) {
    this.restApi = restApi;

    this.mediaTypeTransitions = Arrays.stream(restApi.transition())
      .collect(Collectors.groupingBy(transition -> transition.context().name()));

    buildHandler(API, "/", permissionAll(), this::handleApi);
    buildHandlers.accept(this);
  }

  private RestAuthorization permissionAll() {
    return context -> true;
  }

  protected void handleApi(final RequestContext requestContext) {
    requestContext.json(restApi);
  }

  public void buildHandler(
    final String transitionName,
    final String path,
    final RestAuthorization restAuthorization,
    final Consumer<RequestContext> process
  ) {
    restApi.getTransition(transitionName).ifPresent(
      transition -> createHandler(transition, path, restAuthorization)
        .setProcess(process)
    );
  }

  public void buildHandler(
    final String transitionName,
    final String path,
    final RestAuthorization restAuthorization,
    final Consumer<RequestContext> processHead,
    final Consumer<RequestContext> process
  ) {
    restApi.getTransition(transitionName).ifPresent(
      transition -> createHandler(transition, path, restAuthorization)
        .setProcessHead(processHead)
        .setProcess(process)
    );
  }

  public RestServerHandler<RoutingContext> createHandler(
    final Transition transition,
    final String path,
    final RestAuthorization restAuthorization
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
      restAuthorization,
      transition);
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

  @Override
  public RestAuthorization createPermissionBased(final String permission) {
    return new VertxRestAuthorization(
      PermissionBasedAuthorization.create(permission)
    );
  }
}
