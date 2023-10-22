package io.github.enpassant.jrestful.example.account;

import io.vertx.ext.web.handler.HttpException;

import java.util.*;
import java.util.stream.Collectors;

public class AccountManager {
  private final Map<AccountNumber, Account> accountMap = new HashMap<>();

  public List<Account> findAll() {
    return accountMap.values().stream().toList();
  }

  public Optional<Account> getAccount(final AccountNumber accountNumber) {
    if (accountMap.containsKey(accountNumber)) {
      return Optional.of(accountMap.get(accountNumber));
    } else {
      return Optional.empty();
    }
  }

  public Account addAccount(final AccountNumber accountNumber, final Name name) {
    if (accountMap.containsKey(accountNumber)) {
      return accountMap.get(accountNumber);
    } else {
      final Account account = new Account(name, accountNumber, 0.0);
      accountMap.put(accountNumber, account);
      return account;
    }
  }

  public Optional<Account> deleteAccount(final AccountNumber accountNumber) {
    if (accountMap.containsKey(accountNumber)) {
      return Optional.of(accountMap.remove(accountNumber));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Account> modifyAccountName(final AccountNumber accountNumber, final ChangeName changeName) {
    if (accountMap.containsKey(accountNumber)) {
      final Account account = accountMap.get(accountNumber);
      if (account.name().equals(changeName.currentName())) {
        final Name name = changeName.newName();
        final Account modifiedAccount = new Account(name, accountNumber, account.balance());
        accountMap.put(accountNumber, modifiedAccount);
        return Optional.of(modifiedAccount);
      }
    }
    return Optional.empty();
  }

  public Optional<Account> deposit(final AccountNumber accountNumber, final Deposit deposit) {
    if (accountMap.containsKey(accountNumber)) {
      final Account account = accountMap.get(accountNumber);
      final Account modifiedAccount = new Account(
        account.name(),
        accountNumber,
        account.balance() + deposit.amount()
      );
      accountMap.put(accountNumber, modifiedAccount);
      return Optional.of(modifiedAccount);
    } else {
      return Optional.empty();
    }
  }

  public Optional<Account> withdraw(final AccountNumber accountNumber, final Withdraw withdraw) {
    if (accountMap.containsKey(accountNumber)) {
      final Account account = accountMap.get(accountNumber);
      final double balance = account.balance() - withdraw.amount();
      if (balance < 0.0) {
        throw new HttpException(400, "Withdraw is not possible because the balance would be negative");
      }
      final Account modifiedAccount = new Account(
        account.name(),
        accountNumber,
        balance
      );
      accountMap.put(accountNumber, modifiedAccount);
      return Optional.of(modifiedAccount);
    } else {
      return Optional.empty();
    }
  }

  public AccountNumber makeNewAccountNumber() {
    final Random random = new Random();
    final String number = random.ints(24, 0, 10)
      .mapToObj(Integer::toString)
      .collect(Collectors.joining());
    return new AccountNumber(number);
  }
}
