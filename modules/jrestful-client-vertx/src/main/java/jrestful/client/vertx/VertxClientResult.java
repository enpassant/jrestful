package jrestful.client.vertx;

import io.vertx.core.Future;
import jrestful.client.ClientResult;
import jrestful.fp.Tuple2;

import java.util.function.Consumer;
import java.util.function.Function;

public class VertxClientResult<T> implements ClientResult<T> {
  private final Future<T> future;

  public VertxClientResult(final Future<T> future) {
    this.future = future;
  }

  @Override
  public ClientResult<T> onSuccess(final Consumer<T> handler) {
    final Future<T> nextFuture = future.onSuccess(handler::accept);
    return new VertxClientResult<>(nextFuture);
  }

  @Override
  public ClientResult<T> onFailure(final Consumer<Throwable> handler) {
    final Future<T> nextFuture = future.onFailure(handler::accept);
    return new VertxClientResult<>(nextFuture);
  }

  @Override
  public <R> ClientResult<R> andThen(final Function<T, ClientResult<R>> fn) {
    final Future<R> composedFuture = future.compose(t -> {
      final VertxClientResult<R> result = (VertxClientResult<R>) fn.apply(t);
      return result.future;
    });
    return new VertxClientResult<>(composedFuture);
  }

  @Override
  public <R> ClientResult<Tuple2<T, R>> join(final ClientResult<R> that) {
    final VertxClientResult<R> thatResult = (VertxClientResult<R>) that;
    return new VertxClientResult<>(
      Future.join(future, thatResult.future)
        .map(cf -> Tuple2.of(cf.resultAt(0), cf.resultAt(1)))
    );
  }

  @Override
  public ClientResult<T> handle(final Consumer<ClientResult<T>> handler) {
    handler.accept(this);
    return this;
  }
}
