package jrestful.client;

import jrestful.fp.Either;
import jrestful.fp.Left;
import jrestful.fp.Right;
import jrestful.fp.Tuple2;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ClientResult<T> {

  ClientResult<T> onSuccess(Consumer<T> handler);

  ClientResult<T> onFailure(Consumer<Throwable> handler);

  <R> ClientResult<R> andThen(Function<T, ClientResult<R>> fn);

  ClientResult<T> recover(Function<Throwable, ClientResult<T>> fn);

  default ClientResult<T> onComplete(final Consumer<Either<Throwable, T>> handler) {
    return this.onSuccess(t -> handler.accept(Right.of(t)))
      .onFailure(throwable -> handler.accept(Left.of(throwable)));
  }

  <R> ClientResult<Tuple2<T, R>> join(ClientResult<R> that);

  ClientResult<T> handle(Consumer<ClientResult<T>> handler);

  T result();
}
