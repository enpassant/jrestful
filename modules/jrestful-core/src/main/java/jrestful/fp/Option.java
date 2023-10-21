package jrestful.fp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = None.class, name = "None"),
  @JsonSubTypes.Type(value = Some.class, name = "Some")
})
public interface Option<T> {
  @JsonIgnore
  boolean isEmpty();

  @JsonIgnore
  boolean isPresent();

  Stream<T> stream();

  <R> Option<R> map(Function<T, R> fn);

  T orElse(T newValue);

  void ifPresent(Consumer<T> handler);
}

