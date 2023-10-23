package jrestful.server.vertx;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import jrestful.Transition;
import jrestful.link.Link;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;
import jrestful.server.RestServerHandler;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VertxRestServerHandler implements Handler<RoutingContext>, RestServerHandler<RoutingContext> {
  private static final Logger LOGGER = Logger.getLogger(VertxRestServerHandler.class.getName());
  private final VertxRestServer vertxRestServer;
  private final String contentType;
  private final String rel;
  private final RestAuthorization restAuthorization;
  private final Transition transition;

  private List<Link> headerLinks = List.of();

  private Consumer<RequestContext> processHead = (routingContext) -> {
  };

  private Consumer<RequestContext> process;

  public VertxRestServerHandler(
    final VertxRestServer vertxRestServer,
    final String contentType,
    final String rel,
    final RestAuthorization restAuthorization,
    final Transition transition
  ) {
    this.vertxRestServer = vertxRestServer;
    this.contentType = contentType;
    this.rel = rel;
    this.restAuthorization = restAuthorization;
    this.transition = transition;
  }

  public RestServerHandler<RoutingContext> setProcessHead(final Consumer<RequestContext> processHead) {
    this.processHead = processHead;
    return this;
  }

  public RestServerHandler<RoutingContext> setProcess(final Consumer<RequestContext> process) {
    this.process = process;
    return this;
  }

  @Override
  public void handle(final RoutingContext routingContext) {
    final HttpServerResponse response = routingContext.response();
    final User user = routingContext.user();
    final RequestContext requestContext = new VertxRequestContext(routingContext);
    if (user != null) {
      if (!restAuthorization.match(requestContext)) {
        response.setStatusCode(403).end("Forbidden");
        return;
      } else {
        final String accessToken = user.principal().getString("access_token");
        response.putHeader(HttpHeaders.AUTHORIZATION.toString(), "Bearer " + accessToken);
      }
    }
    response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
    headerLinks = vertxRestServer.getLinks(contentType);
    processHead.accept(requestContext);
    putHeaderLink(headerLinks, response, requestContext, user);

    final HttpMethod method = routingContext.request().method();
    LOGGER.fine(() -> MessageFormat.format("Process {0}", method));
    if (!method.equals(HttpMethod.HEAD)) {
      process.accept(requestContext);
    } else {
      routingContext.end();
    }
  }

  public void putHeaderLink(
    final List<Link> headerLinks,
    final HttpServerResponse response,
    final RequestContext requestContext,
    final User user
  ) {
    final String linkStr = headerLinks.stream()
      .filter(link -> user == null || restAuthorization.match(requestContext))
      .map(link -> link.toWebLink(requestContext.qetQueryString(link.relLink().rel())))
      .collect(Collectors.joining(", "));
    response.putHeader("Link", linkStr);
  }
}
