package jrestful.fp;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public record None<T>() implements Option<T> {
  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isPresent() {
    return false;
  }

  @Override
  public Stream stream() {
    return Stream.empty();
  }

  @Override
  public Option map(final Function fn) {
    return this;
  }

  @Override
  public T orElse(final T newValue) {
    return newValue;
  }

  @Override
  public void ifPresent(final Consumer<T> handler) {
  }
}
