package jrestful.server;

import java.util.function.BiConsumer;

public interface RestServerHandler<T> {

  RestServerHandler<T> setProcessHead(final BiConsumer<RestServerHandler<T>, RequestContext<T>> processHead);

  RestServerHandler<T> setProcess(final BiConsumer<RestServerHandler<T>, RequestContext<T>> process);
}
