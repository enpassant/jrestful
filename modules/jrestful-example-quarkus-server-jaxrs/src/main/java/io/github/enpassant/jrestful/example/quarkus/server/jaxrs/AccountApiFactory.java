package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import jrestful.RestApi;
import jrestful.Transition;
import jrestful.link.RelLink;
import jrestful.server.RestServer;
import jrestful.type.MediaType;
import jrestful.type.TypeList;
import jrestful.type.TypeObject;
import jrestful.type.Types;

import static io.github.enpassant.jrestful.example.quarkus.api.AccountApi.*;
import static io.github.enpassant.jrestful.example.quarkus.server.jaxrs.AccountResource.*;

final class AccountApiFactory {
  private AccountApiFactory() { }

  static RestApi create(final RestServer server) {
    final MediaType account = new MediaType(ACCOUNT,
      new TypeObject<>("Account", "(name: Name, accountNumber: AccountNumber, balance: Double)"));
    final MediaType accountList = new MediaType(ACCOUNT_LIST, new TypeList<>(account));
    final MediaType name = new MediaType(NAME, new TypeObject<>("Name", "String"));
    final MediaType changeName = new MediaType(CHANGE_NAME,
      new TypeObject<>("ChangeName", "(currentName: Name, newName: Name)"));
    final MediaType deposit = new MediaType(DEPOSIT,
      new TypeObject<>("Deposit", "(sourceAccount: AccountNumber, amount: Double)"));
    final MediaType withdraw = new MediaType(WITHDRAW,
      new TypeObject<>("Withdraw", "(targetAccount: AccountNumber, amount: Double)"));
    final Types types = new Types(server.api, account, accountList, name, changeName, deposit, withdraw,
      new TypeObject<>("AccountNumber", "String"));

    return new RestApi(types, server.apiTransition,
      new Transition(LIST_ACCOUNTS, server.api, RelLink.get(REL_ACCOUNTS, ACCOUNT_LIST)),
      new Transition(NEW_ACCOUNT, accountList, RelLink.put(REL_NEW, NAME, ACCOUNT)),
      new Transition(GET_ACCOUNT, accountList, RelLink.get(REL_ITEM, ACCOUNT)),
      new Transition(DELETE_ACCOUNT, account, RelLink.delete(REL_DELETE, ACCOUNT)),
      new Transition(CHANGE_ACCOUNT_NAME, account, RelLink.put(REL_CHANGE_NAME, NAME, ACCOUNT)),
      new Transition(CHANGE_ACCOUNT_NAME_IF_CURRENT, account, RelLink.put(REL_CHANGE_NAME, CHANGE_NAME, ACCOUNT)),
      new Transition(DEPOSIT_ACCOUNT, account, RelLink.put(REL_DEPOSIT, DEPOSIT, ACCOUNT)),
      new Transition(WITHDRAW_ACCOUNT, account, RelLink.put(REL_WITHDRAW, WITHDRAW, ACCOUNT)));
  }
}
