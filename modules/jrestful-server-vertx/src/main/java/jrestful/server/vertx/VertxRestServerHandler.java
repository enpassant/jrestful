package jrestful.server.vertx;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.web.RoutingContext;
import jrestful.Transition;
import jrestful.link.Link;
import jrestful.server.RequestContext;
import jrestful.server.RestServerHandler;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VertxRestServerHandler implements Handler<RoutingContext>, RestServerHandler<RoutingContext> {
  private static final Logger LOGGER = Logger.getLogger(VertxRestServerHandler.class.getName());
  private final VertxRestServer vertxRestServer;
  private final String contentType;
  private final String rel;
  private final Authorization authorization;
  private final Transition transition;

  private List<Link> headerLinks = List.of();

  private BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> processHead =
    (restHandler, routingContext) -> {
    };

  private BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> process;

  public VertxRestServerHandler(
    final VertxRestServer vertxRestServer,
    final String contentType,
    final String rel,
    final Authorization authorization,
    final Transition transition
  ) {
    this.vertxRestServer = vertxRestServer;
    this.contentType = contentType;
    this.rel = rel;
    this.authorization = authorization;
    this.transition = transition;
  }

  public RestServerHandler<RoutingContext> setProcessHead(final BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> processHead) {
    this.processHead = processHead;
    return this;
  }

  public RestServerHandler<RoutingContext> setProcess(final BiConsumer<RestServerHandler<RoutingContext>, RequestContext<RoutingContext>> process) {
    this.process = process;
    return this;
  }

  @Override
  public void handle(final RoutingContext routingContext) {
    final HttpServerResponse response = routingContext.response();
    final User user = routingContext.user();
    if (user != null) {
      if (!authorization.match(user)) {
        response.setStatusCode(403).end("Forbidden");
        return;
      } else {
        final String accessToken = user.principal().getString("access_token");
        response.putHeader(HttpHeaders.AUTHORIZATION.toString(), "Bearer " + accessToken);
      }
    }
    response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
    headerLinks = vertxRestServer.getLinks(contentType);
    final RequestContext requestContext = new RequestContext(routingContext);
    processHead.accept(this, requestContext);
    putHeaderLink(headerLinks, response, requestContext, user);

    final HttpMethod method = routingContext.request().method();
    LOGGER.fine(() -> MessageFormat.format("Process {0}", method));
    if (!method.equals(HttpMethod.HEAD)) {
      process.accept(this, requestContext);
    } else {
      routingContext.end();
    }
  }

  public void putHeaderLink(
    final List<Link> headerLinks,
    final HttpServerResponse response,
    final RequestContext<RoutingContext> requestContext,
    final User user
  ) {
    final String linkStr = headerLinks.stream()
      .filter(link -> user == null || authorization.match(user))
      .map(link -> link.toWebLink(requestContext.qetQueryString(link.relLink().rel())))
      .collect(Collectors.joining(", "));
    response.putHeader("Link", linkStr);
  }
}
