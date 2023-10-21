package io.github.enpassant.jrestful.example.account;

import io.github.enpassant.jrestful.example.starter.VertxAuthenticate;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.link.Link;
import jrestful.link.RelLink;
import jrestful.server.RestServer;
import jrestful.server.RestServerHandler;
import jrestful.server.vertx.VertxRestServer;
import jrestful.type.MediaType;
import jrestful.type.TypeList;
import jrestful.type.TypeObject;
import jrestful.type.Types;

import java.util.List;
import java.util.Optional;

public class AccountRestServer implements AccountMediaTypes {

  public static final String LIST_ACCOUNTS = "listAccounts";
  public static final String NEW_ACCOUNT = "newAccount";
  public static final String GET_ACCOUNT = "getAccount";
  public static final String DELETE_ACCOUNT = "deleteAccount";
  public static final String MODIFY_ACCOUNT_NAME = "modifyAccountName";
  public static final String DEPOSIT = "deposit";
  public static final String WITHDRAW = "withdraw";

  private final AccountManager accountManager;
  private final VertxAuthenticate vertxAuthenticate;
  private final RestServer<RoutingContext, Authorization> restServer;

  private final static Authorization permissionUser =
    PermissionBasedAuthorization.create("user");
  private final static Authorization permissionAdmin =
    PermissionBasedAuthorization.create("admin");

  public AccountRestServer(
    final Router router,
    final AccountManager accountManager,
    final VertxAuthenticate vertxAuthenticate
  ) {
    restServer = new VertxRestServer(router);

    this.accountManager = accountManager;
    this.vertxAuthenticate = vertxAuthenticate;

    final RestApi restApi = createRestApi();
    restServer.init(restApi, this::buildHandlers);
  }

  protected RestApi createRestApi() {
    final var account = new MediaType(
      APPLICATION_ACCOUNT_JSON,
      new TypeObject<>("Account", "(name: Name, number: AccountNumber, balance: Double)")
    );
    final var accountList = new MediaType(
      APPLICATION_LIST_ACCOUNT_JSON,
      new TypeList<>(account)
    );
    final var name = new MediaType(
      APPLICATION_NAME_JSON,
      new TypeObject<>("Name", "(value: String[80])")
    );
    final var changeName = new MediaType(
      APPLICATION_CHANGE_NAME_JSON,
      new TypeObject<>("ChangeName", "(currentName: Name, newName: Name)")
    );
    final var deposit = new MediaType(
      APPLICATION_DEPOSIT_JSON,
      new TypeObject<>("Deposit", "(sourceAccount: AccountNumber, amount: Double)")
    );
    final var withdraw = new MediaType(
      APPLICATION_WITHDRAW_JSON,
      new TypeObject<>("Withdraw", "(targetAccount: AccountNumber, amount: Double)")
    );

    final Types types = new Types(restServer.api, account, accountList, changeName, deposit, withdraw, name,
      new TypeObject<>("AccountNumber", "(value: String[80]")
    );

    return new RestApi(
      types,
      restServer.apiTransition,
      new Transition(LIST_ACCOUNTS, restServer.api, RelLink.get("accounts", APPLICATION_LIST_ACCOUNT_JSON)),
      new Transition(NEW_ACCOUNT, accountList, RelLink.put("new", APPLICATION_NAME_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(GET_ACCOUNT, accountList, RelLink.get("item", APPLICATION_ACCOUNT_JSON)),
      new Transition(DELETE_ACCOUNT, account, RelLink.delete("delete", APPLICATION_ACCOUNT_JSON)),
      new Transition(MODIFY_ACCOUNT_NAME, account, RelLink.put("edit", APPLICATION_CHANGE_NAME_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(DEPOSIT, account, RelLink.put("edit", APPLICATION_DEPOSIT_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(WITHDRAW, account, RelLink.put("edit", APPLICATION_WITHDRAW_JSON, APPLICATION_ACCOUNT_JSON))
    );
  }

  protected void buildHandlers(final RestServer<RoutingContext, Authorization> restServer) {
    restServer.buildHandler(LIST_ACCOUNTS, "/auth/accounts", permissionUser, this::handleListAccountsHead, this::handleListAccounts);
    restServer.buildHandler(NEW_ACCOUNT, "/auth/account/:accountNumber", permissionUser, this::handleAccountHead, this::handleNewAccount);
    restServer.buildHandler(GET_ACCOUNT, "/auth/account/:accountNumber", permissionUser, this::handleAccountHead, this::handleGetAccount);
    restServer.buildHandler(DELETE_ACCOUNT, "/auth/account/:accountNumber", permissionAdmin, this::handleAccountHead, this::handleDeleteAccount);
    restServer.buildHandler(MODIFY_ACCOUNT_NAME, "/auth/account/:accountNumber", permissionAdmin, this::handleAccountHead, this::handleModifyAccountName);
    restServer.buildHandler(DEPOSIT, "/auth/account/:accountNumber", permissionUser, this::handleAccountHead, this::handleDeposit);
    restServer.buildHandler(WITHDRAW, "/auth/account/:accountNumber", permissionUser, this::handleAccountHead, this::handleWithdraw);
  }

  private void handleListAccountsHead(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    restServerHandler.changeLink(link -> {
      if (link.relLink().rel().equalsIgnoreCase("new")) {
        final String number = accountManager.makeNewAccountNumber().value();
        return new Link(link.path().replaceAll(":accountNumber", number), link.relLink());
      } else {
        return link;
      }
    });
  }

  private void handleListAccounts(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    final List<Account> accounts = accountManager.findAll();
    routingContext.json(accounts);
  }

  private void handleAccountHead(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    restServerHandler.changeLink(link -> {
      final String number = routingContext.pathParam("accountNumber");
      return new Link(link.path().replaceAll(":accountNumber", number), link.relLink());
    });
  }

  private void handleNewAccount(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    restServer.parseBodyAs(routingContext, Name.class).ifPresent(name -> {
      final String number = routingContext.pathParam("accountNumber");
      final AccountNumber accountNumber = new AccountNumber(number);
      final Account addedAccount = accountManager.addAccount(accountNumber, name);
      final HttpServerResponse response = routingContext.response();
      if (addedAccount.name().equals(name)) {
        response.setStatusCode(201);
        routingContext.json(addedAccount);
      } else {
        response.setStatusCode(409).end("Conflict");
      }
    });
  }

  private void handleGetAccount(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    final String number = routingContext.pathParam("accountNumber");
    final AccountNumber accountNumber = new AccountNumber(number);
    final Optional<Account> accountOptional = accountManager.getAccount(accountNumber);
    if (accountOptional.isPresent()) {
      final Account account = accountOptional.get();
      routingContext.json(account);
    } else {
      final HttpServerResponse response = routingContext.response();
      response.setStatusCode(404).end("Not found");
    }
  }

  private void handleDeleteAccount(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    final String number = routingContext.pathParam("accountNumber");
    final AccountNumber accountNumber = new AccountNumber(number);
    final Optional<Account> accountOptional = accountManager.deleteAccount(accountNumber);
    if (accountOptional.isPresent()) {
      final Account account = accountOptional.get();
      routingContext.json(account);
    } else {
      final HttpServerResponse response = routingContext.response();
      response.setStatusCode(404).end("Not found");
    }
  }

  private void handleModifyAccountName(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    restServer.parseBodyAs(routingContext, ChangeName.class).ifPresent(changeName -> {
      final String number = routingContext.pathParam("accountNumber");
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.modifyAccountName(accountNumber, changeName);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        routingContext.json(account);
      } else {
        final HttpServerResponse response = routingContext.response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }

  private void handleDeposit(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    restServer.parseBodyAs(routingContext, Deposit.class).ifPresent(deposit -> {
      final String number = routingContext.pathParam("accountNumber");
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.deposit(accountNumber, deposit);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        routingContext.json(account);
      } else {
        final HttpServerResponse response = routingContext.response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }

  private void handleWithdraw(final RestServerHandler<RoutingContext> restServerHandler, final RoutingContext routingContext) {
    restServer.parseBodyAs(routingContext, Withdraw.class).ifPresent(withdraw -> {
      final String number = routingContext.pathParam("accountNumber");
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.withdraw(accountNumber, withdraw);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        routingContext.json(account);
      } else {
        final HttpServerResponse response = routingContext.response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }
}
