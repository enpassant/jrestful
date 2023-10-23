package jrestful.fp;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Right<L, R> implements Either<L, R> {
  private final R value;

  private Right(final R r) {
    value = r;
  }

  public static <L, R> Right<L, R> of(final R r) {
    return new Right<>(r);
  }

  @Override
  public <B> Either<L, B> map(final Function<R, B> f) {
    return new Right<>(f.apply(value));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <B> Either<B, R> mapLeft(final Function<L, B> f) {
    return (Either<B, R>) this;
  }

  @Override
  public <B> Either<L, B> flatMap(final Function<R, Either<L, B>> f) {
    return f.apply(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <B> Either<B, R> flatMapLeft(final Function<L, Either<B, R>> f) {
    return (Either<B, R>) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <B> Either<L, B> recover(final Function<L, B> f) {
    return (Either<L, B>) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <B> Either<L, B> flatten() {
    return (Either<L, B>) value;
  }

  @Override
  public Either<L, R> forEachLeft(final Consumer<L> f) {
    return this;
  }

  @Override
  public Either<L, R> forEach(final Consumer<R> f) {
    f.accept(value);
    return this;
  }

  @Override
  public R orElse(final R value) {
    return this.value;
  }

  @Override
  public L left() {
    throw new NoSuchElementException("No value present");
  }

  @Override
  public R right() {
    return value;
  }

  @Override
  public boolean isLeft() {
    return false;
  }

  @Override
  public boolean isRight() {
    return true;
  }

  @Override
  public Either<R, L> swap() {
    return Left.of(value);
  }

  @Override
  public R get() {
    return value;
  }

  @Override
  public <B> B fold(final Function<L, B> fnLeft, final Function<R, B> fnRight) {
    return fnRight.apply(value);
  }

  @Override
  public String toString() {
    return "Right(" + value + ")";
  }

  @Override
  public boolean equals(final Object value) {
    if (value == this) {
      return true;
    }
    if (value instanceof Right) {
      @SuppressWarnings("unchecked") final Right<L, R> valueRight = (Right<L, R>) value;
      return this.value.equals(valueRight.right());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
