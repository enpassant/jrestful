package jrestful.fp;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public record Some<T>(T value) implements Option<T> {
  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public Stream<T> stream() {
    return Stream.of(value);
  }

  @Override
  public <R> Option<R> map(final Function<T, R> fn) {
    return new Some<>(fn.apply(value));
  }

  @Override
  public T orElse(final T newValue) {
    return value;
  }

  @Override
  public void ifPresent(final Consumer<T> handler) {
    handler.accept(value);
  }
}
