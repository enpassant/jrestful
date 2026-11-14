package io.github.enpassant.jrestful.example.quarkus.client;

import io.github.enpassant.jrestful.example.quarkus.api.Account;
import io.github.enpassant.jrestful.example.quarkus.api.ChangeName;
import io.github.enpassant.jrestful.example.quarkus.api.Deposit;
import io.github.enpassant.jrestful.example.quarkus.api.Name;
import io.github.enpassant.jrestful.example.quarkus.api.Withdraw;

import java.util.Map;

import static io.github.enpassant.jrestful.example.quarkus.api.AccountApi.*;

final class AccountClientTypes {
  static final Map<String, Class<?>> KNOWN_TYPES = Map.of(
    ACCOUNT, Account.class,
    NAME, Name.class,
    CHANGE_NAME, ChangeName.class,
    DEPOSIT, Deposit.class,
    WITHDRAW, Withdraw.class
  );

  private AccountClientTypes() {
  }
}
