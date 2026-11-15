package io.github.enpassant.jrestful.example.quarkus.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record AccountNumber(String value) {

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public AccountNumber {
  }

  @Override
  @JsonValue
  public String value() {
    return value;
  }
}
