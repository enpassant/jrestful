package jrestful.server.vertx;

import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;

public class VertxRestAuthorization implements RestAuthorization {
  private final Authorization authorization;

  public VertxRestAuthorization(final Authorization authorization) {
    this.authorization = authorization;
  }

  @Override
  public boolean match(final RequestContext context) {
    if (context instanceof final VertxRequestContext vertxRequestContext) {
      final User user = vertxRequestContext.getUser();
      return authorization.match(user);
    }
    return false;
  }
}
