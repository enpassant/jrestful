package jrestful.server.vertx;

import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;

public class VertxRestAuthorization implements RestAuthorization {
  private final Authorization authorization;
  private final String permission;

  public VertxRestAuthorization(final Authorization authorization, final String permission) {
    this.authorization = authorization;
    this.permission = permission;
  }

  @Override
  public boolean match(final RequestContext context) {
    if (context instanceof final VertxRequestContext vertxRequestContext) {
      final User user = vertxRequestContext.getUser();
      if (user == null) {
        return false;
      }
      if (authorization.match(user)) {
        return true;
      }
      return Boolean.TRUE.equals(user.isAuthorized(permission).result());
    }
    return false;
  }
}
