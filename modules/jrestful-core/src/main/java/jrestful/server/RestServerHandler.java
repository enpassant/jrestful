package jrestful.server;

import java.util.function.Consumer;

public interface RestServerHandler<T> {

  RestServerHandler<T> setProcessHead(final Consumer<RequestContext> processHead);

  RestServerHandler<T> setProcess(final Consumer<RequestContext> process);
}
