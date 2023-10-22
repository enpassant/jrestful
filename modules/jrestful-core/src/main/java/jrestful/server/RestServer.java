package jrestful.server;

import jrestful.RestApi;
import jrestful.Root;
import jrestful.Transition;
import jrestful.link.RelLink;
import jrestful.type.MediaType;
import jrestful.type.TypeObject;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface RestServer<T, A> {
  String API = "api";

  String MT_API = "application/RestAPI+json";
  MediaType api = new MediaType(
    MT_API,
    new TypeObject<>("RESTful API", "")
  );

  Transition apiTransition = new Transition(API, new Root(), RelLink.get("", MT_API));

  void init(final RestApi restApi, final Consumer<RestServer<T, A>> buildHandlers);

  void buildHandler(
    final String transitionName,
    final String path,
    final A authorization,
    final BiConsumer<RestServerHandler<T>, RequestContext<T>> process
  );

  void buildHandler(
    final String transitionName,
    final String path,
    final A authorization,
    final BiConsumer<RestServerHandler<T>, RequestContext<T>> processHead,
    final BiConsumer<RestServerHandler<T>, RequestContext<T>> process
  );

  <R> Optional<R> parseBodyAs(final RequestContext<T> requestContext, final Class<R> clazz);
}
