package jrestful.server;

public interface RestAuthorization {
  boolean match(final RequestContext context);
}
