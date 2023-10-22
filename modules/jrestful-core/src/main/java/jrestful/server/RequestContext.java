package jrestful.server;

public class RequestContext<T> {
  private final T context;

  public RequestContext(final T context) {
    this.context = context;
  }

  public T getContext() {
    return context;
  }
}
