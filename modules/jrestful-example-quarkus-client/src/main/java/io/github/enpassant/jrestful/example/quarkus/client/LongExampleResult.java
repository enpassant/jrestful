package io.github.enpassant.jrestful.example.quarkus.client;

import io.github.enpassant.jrestful.example.quarkus.api.Account;

public record LongExampleResult(
  Account deletedAccount,
  Account finalAccount,
  long elapsedMillis
) {
}
