package io.github.enpassant.jrestful.example.quarkus.api;

public record Withdraw(AccountNumber targetAccount, Double amount) {
}
