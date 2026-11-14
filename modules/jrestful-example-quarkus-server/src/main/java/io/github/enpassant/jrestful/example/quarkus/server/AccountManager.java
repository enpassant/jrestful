package io.github.enpassant.jrestful.example.quarkus.server;

import io.github.enpassant.jrestful.example.quarkus.api.Account;
import io.github.enpassant.jrestful.example.quarkus.api.AccountNumber;
import io.github.enpassant.jrestful.example.quarkus.api.ChangeName;
import io.github.enpassant.jrestful.example.quarkus.api.Deposit;
import io.github.enpassant.jrestful.example.quarkus.api.Name;
import io.github.enpassant.jrestful.example.quarkus.api.Withdraw;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountManager {
  private final Map<AccountNumber, Account> accounts = new LinkedHashMap<>();

  public Collection<Account> findAll() {
    return accounts.values();
  }

  public Optional<Account> get(final AccountNumber number) {
    return Optional.ofNullable(accounts.get(number));
  }

  public Account add(final AccountNumber number, final Name name) {
    return accounts.computeIfAbsent(number, ignored -> new Account(name, number, 0.0));
  }

  public Optional<Account> delete(final AccountNumber number) {
    return Optional.ofNullable(accounts.remove(number));
  }

  public Optional<Account> changeName(final AccountNumber number, final Name name) {
    return get(number).map(account -> replace(account, name, account.balance()));
  }

  public Optional<Account> changeName(final AccountNumber number, final ChangeName change) {
    return get(number)
      .filter(account -> account.name().equals(change.currentName()))
      .map(account -> replace(account, change.newName(), account.balance()));
  }

  public Optional<Account> deposit(final AccountNumber number, final Deposit deposit) {
    return get(number).map(account -> replace(account, account.name(), account.balance() + deposit.amount()));
  }

  public Optional<Account> withdraw(final AccountNumber number, final Withdraw withdraw) {
    return get(number)
      .filter(account -> account.balance() >= withdraw.amount())
      .map(account -> replace(account, account.name(), account.balance() - withdraw.amount()));
  }

  public AccountNumber nextNumber() {
    return new AccountNumber(UUID.randomUUID().toString());
  }

  private Account replace(final Account oldAccount, final Name name, final double balance) {
    final Account replacement = new Account(name, oldAccount.accountNumber(), balance);
    accounts.put(replacement.accountNumber(), replacement);
    return replacement;
  }
}
