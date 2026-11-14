package io.github.enpassant.jrestful.example.quarkus.client;

import io.github.enpassant.jrestful.example.quarkus.api.Account;

public record SagaExampleResult(
  Account otpAccount,
  Account cibAccount,
  double amount,
  boolean compensated,
  long elapsedMillis
) {
}
