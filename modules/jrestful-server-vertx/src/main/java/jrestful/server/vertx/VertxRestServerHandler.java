package jrestful.server.vertx;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.web.RoutingContext;
import jrestful.link.Link;
import jrestful.link.RelLink;
import jrestful.server.RestServerHandler;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VertxRestServerHandler implements Handler<RoutingContext>, RestServerHandler<RoutingContext> {
  private static final Logger LOGGER = Logger.getLogger(VertxRestServerHandler.class.getName());
  private final VertxRestServer vertxRestServer;
  private final String contentType;
  private final String rel;
  private final Authorization authorization;

  private List<Link> headerLinks = List.of();

  private BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> processHead =
    (restHandler, routingContext) -> {
    };

  private BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> process;

  public VertxRestServerHandler(
    final VertxRestServer vertxRestServer,
    final String contentType,
    final String rel,
    final Authorization authorization
  ) {
    this.vertxRestServer = vertxRestServer;
    this.contentType = contentType;
    this.rel = rel;
    this.authorization = authorization;
  }

  public RestServerHandler<RoutingContext> setProcessHead(final BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> processHead) {
    this.processHead = processHead;
    return this;
  }

  public RestServerHandler<RoutingContext> setProcess(final BiConsumer<RestServerHandler<RoutingContext>, RoutingContext> process) {
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
    processHead.accept(this, routingContext);
    putHeaderLink(headerLinks, response, user);

    final HttpMethod method = routingContext.request().method();
    LOGGER.fine(() -> MessageFormat.format("Process {0}", method));
    if (!method.equals(HttpMethod.HEAD)) {
      process.accept(this, routingContext);
    } else {
      routingContext.end();
    }
  }

  public void putHeaderLink(
    final List<Link> headerLinks,
    final HttpServerResponse response,
    final User user
  ) {
    final String linkStr = headerLinks.stream()
      .filter(link -> user == null || authorization.match(user))
      .map(l -> l.uri() + "; rel=\"" + l.relLink().rel() + "\"")
      .collect(Collectors.joining(", "));
    response.putHeader("Link", linkStr);
  }

  public void changeLink(final String relName, final String param, final Supplier<String> value) {
    this.headerLinks = headerLinks.stream()
      .map(link -> {
          final RelLink relLink = link.relLink();
          final String rel = relLink.rel();
          return rel.equalsIgnoreCase(relName) ?
            new Link(link.uri().replaceAll(param, value.get()), relLink) :
            link;
        }
      ).collect(Collectors.toList());
  }
}
