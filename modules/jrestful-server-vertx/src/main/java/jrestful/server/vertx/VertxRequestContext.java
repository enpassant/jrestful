package jrestful.server.vertx;

import io.vertx.ext.web.RoutingContext;
import jrestful.server.RequestContext;

public class VertxRequestContext extends RequestContext<RoutingContext> {
  public VertxRequestContext(final RoutingContext context) {
    super(context);
  }
}
