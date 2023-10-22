package jrestful.client;

import jrestful.fp.Tuple2;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ClientResult<T> {

  ClientResult<T> onSuccess(Consumer<T> handler);

  ClientResult<T> onFailure(Consumer<Throwable> handler);

  <R> ClientResult<R> andThen(Function<T, ClientResult<R>> fn);

  ClientResult<T> recover(Function<Throwable, ClientResult<T>> fn);

  default ClientResult<T> onComplete(
    final Consumer<T> successHandler,
    final Consumer<Throwable> failureHandler
  ) {
    return this.onSuccess(successHandler)
      .onFailure(failureHandler);
  }

  <R> ClientResult<Tuple2<T, R>> join(ClientResult<R> that);

  ClientResult<T> handle(Consumer<ClientResult<T>> handler);
}
