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

  MediaType api = new MediaType(
    "application/RestAPI+json",
    new TypeObject<>("RESTful API", "RESTful API")
  );

  Transition apiTransition = new Transition(API, new Root(), RelLink.get("", api));

  void init(final RestApi restApi, final Consumer<RestServer<T, A>> buildHandlers);

  void buildHandler(
    final String transitionName,
    final String path,
    final A authorization,
    final BiConsumer<RestServerHandler<T>, T> process
  );

  void buildHandler(
    final String transitionName,
    final String path,
    final A authorization,
    final BiConsumer<RestServerHandler<T>, T> processHead,
    final BiConsumer<RestServerHandler<T>, T> process
  );

  <R> Optional<R> parseBodyAs(final T routingContext, final Class<R> clazz);
}
