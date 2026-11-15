package io.github.enpassant.jrestful.example.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Name(String value) {

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public Name {
  }

  @Override
  @JsonValue
  public String value() {
    return value;
  }
}
