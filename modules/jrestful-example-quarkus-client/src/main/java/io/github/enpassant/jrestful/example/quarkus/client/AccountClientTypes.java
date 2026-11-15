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
    APPLICATION_ACCOUNT_JSON, Account.class,
    APPLICATION_NAME_JSON, Name.class,
    APPLICATION_CHANGE_NAME_JSON, ChangeName.class,
    APPLICATION_DEPOSIT_JSON, Deposit.class,
    APPLICATION_WITHDRAW_JSON, Withdraw.class
  );

  private AccountClientTypes() {
  }
}
