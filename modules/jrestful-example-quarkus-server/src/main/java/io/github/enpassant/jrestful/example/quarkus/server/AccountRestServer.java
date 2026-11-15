package io.github.enpassant.jrestful.example.quarkus.server;

import io.github.enpassant.jrestful.example.quarkus.api.AccountNumber;
import io.github.enpassant.jrestful.example.quarkus.api.ChangeName;
import io.github.enpassant.jrestful.example.quarkus.api.Deposit;
import io.github.enpassant.jrestful.example.quarkus.api.Name;
import io.github.enpassant.jrestful.example.quarkus.api.Withdraw;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.link.RelLink;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;
import jrestful.server.RestServer;
import jrestful.type.MediaType;
import jrestful.type.TypeList;
import jrestful.type.TypeObject;
import jrestful.type.Types;

import static io.github.enpassant.jrestful.example.quarkus.api.AccountApi.*;

public class AccountRestServer {
  private static final String LIST_ACCOUNTS = "listAccounts";
  private static final String NEW_ACCOUNT = "newAccount";
  private static final String GET_ACCOUNT = "getAccount";
  private static final String DELETE_ACCOUNT = "deleteAccount";
  private static final String CHANGE_ACCOUNT_NAME = "changeAccountName";
  private static final String CHANGE_ACCOUNT_NAME_IF_CURRENT = "changeAccountNameIfCurrent";
  private static final String DEPOSIT_ACCOUNT = "depositAccount";
  private static final String WITHDRAW_ACCOUNT = "withdrawAccount";

  private final AccountManager accounts;
  private final RestServer server;
  private final RestAuthorization allowAll = context -> true;

  public AccountRestServer(final RestServer server, final AccountManager accounts) {
    this.server = server;
    this.accounts = accounts;
    server.init(createApi(), this::buildHandlers);
  }

  private RestApi createApi() {
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

    return new RestApi(types,
      server.apiTransition,
      new Transition(LIST_ACCOUNTS, server.api, RelLink.get(REL_ACCOUNTS, ACCOUNT_LIST)),
      new Transition(NEW_ACCOUNT, accountList, RelLink.put(REL_NEW, NAME, ACCOUNT)),
      new Transition(GET_ACCOUNT, accountList, RelLink.get(REL_ITEM, ACCOUNT)),
      new Transition(DELETE_ACCOUNT, account, RelLink.delete(REL_DELETE, ACCOUNT)),
      new Transition(CHANGE_ACCOUNT_NAME, account, RelLink.put(REL_CHANGE_NAME, NAME, ACCOUNT)),
      new Transition(CHANGE_ACCOUNT_NAME_IF_CURRENT, account, RelLink.put(REL_CHANGE_NAME, CHANGE_NAME, ACCOUNT)),
      new Transition(DEPOSIT_ACCOUNT, account, RelLink.put(REL_DEPOSIT, DEPOSIT, ACCOUNT)),
      new Transition(WITHDRAW_ACCOUNT, account, RelLink.put(REL_WITHDRAW, WITHDRAW, ACCOUNT))
    );
  }

  private void buildHandlers(final RestServer restServer) {
    restServer.buildHandler(LIST_ACCOUNTS, "/account", allowAll, this::listHead, this::list);
    restServer.buildHandler(NEW_ACCOUNT, "/account/new", allowAll, this::accountHead, this::create);
    restServer.buildHandler(GET_ACCOUNT, "/account", allowAll, this::accountHead, this::get);
    restServer.buildHandler(DELETE_ACCOUNT, "/account", allowAll, this::accountHead, this::delete);
    restServer.buildHandler(CHANGE_ACCOUNT_NAME, "/account", allowAll, this::accountHead, this::changeName);
    restServer.buildHandler(CHANGE_ACCOUNT_NAME_IF_CURRENT, "/account", allowAll, this::accountHead, this::changeNameIfCurrent);
    restServer.buildHandler(DEPOSIT_ACCOUNT, "/account", allowAll, this::accountHead, this::deposit);
    restServer.buildHandler(WITHDRAW_ACCOUNT, "/account", allowAll, this::accountHead, this::withdraw);
  }

  private void listHead(final RequestContext context) {
    context.addQueryParameter(REL_NEW, "new", "true");
    context.addQueryParameter(REL_NEW, "accountNumber", accounts.nextNumber().value());
  }

  private void accountHead(final RequestContext context) {
    final String number = context.queryParam("accountNumber");
    context.addQueryParameter(REL_DELETE, "accountNumber", number);
    context.addQueryParameter(REL_CHANGE_NAME, "accountNumber", number);
    context.addQueryParameter(REL_DEPOSIT, "accountNumber", number);
    context.addQueryParameter(REL_WITHDRAW, "accountNumber", number);
  }

  private void list(final RequestContext context) {
    context.json(accounts.findAll());
  }

  private void create(final RequestContext context) {
    context.parseBodyAs(Name.class).ifPresent(name ->
      context.json(201, accounts.add(number(context), name)));
  }

  private void get(final RequestContext context) {
    accounts.get(number(context)).ifPresentOrElse(context::json, () -> notFound(context));
  }

  private void delete(final RequestContext context) {
    accounts.delete(number(context)).ifPresentOrElse(context::json, () -> notFound(context));
  }

  private void changeName(final RequestContext context) {
    context.parseBodyAs(Name.class).ifPresent(name ->
      accounts.changeName(number(context), name).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  private void changeNameIfCurrent(final RequestContext context) {
    context.parseBodyAs(ChangeName.class).ifPresent(change ->
      accounts.changeName(number(context), change).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  private void deposit(final RequestContext context) {
    context.parseBodyAs(Deposit.class).ifPresent(deposit ->
      accounts.deposit(number(context), deposit).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  private void withdraw(final RequestContext context) {
    context.parseBodyAs(Withdraw.class).ifPresent(withdraw ->
      accounts.withdraw(number(context), withdraw).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  private AccountNumber number(final RequestContext context) {
    return new AccountNumber(context.queryParam("accountNumber"));
  }

  private void notFound(final RequestContext context) {
    context.sendTextWithCode(404, "Account not found");
  }
}
