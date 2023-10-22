package jrestful.server;

import jrestful.link.Link;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface RestServerHandler<T> {

  RestServerHandler<T> setProcessHead(final BiConsumer<RestServerHandler<T>, RequestContext<T>> processHead);

  RestServerHandler<T> setProcess(final BiConsumer<RestServerHandler<T>, RequestContext<T>> process);

  void changeLink(final String relName, final String param, final Supplier<String> value);

  void changeLink(Function<Link, Link> fn);
}
