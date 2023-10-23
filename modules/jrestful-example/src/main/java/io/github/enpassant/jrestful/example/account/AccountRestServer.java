package io.github.enpassant.jrestful.example.account;

import io.github.enpassant.jrestful.example.starter.VertxAuthenticate;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jrestful.RestApi;
import jrestful.Transition;
import jrestful.link.RelLink;
import jrestful.server.RequestContext;
import jrestful.server.RestServer;
import jrestful.server.vertx.VertxRestServer;
import jrestful.type.MediaType;
import jrestful.type.TypeList;
import jrestful.type.TypeObject;
import jrestful.type.Types;

import java.util.List;
import java.util.Optional;

public class AccountRestServer implements AccountMediaTypes, Relations {

  public static final String LIST_ACCOUNTS = "listAccounts";
  public static final String NEW_ACCOUNT = "newAccount";
  public static final String GET_ACCOUNT = "getAccount";
  public static final String DELETE_ACCOUNT = "deleteAccount";
  public static final String MODIFY_ACCOUNT_NAME = "modifyAccountName";
  public static final String MODIFY_ACCOUNT_NAME_2 = "modifyAccountName2";
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
      new Transition(LIST_ACCOUNTS, restServer.api, RelLink.get(REL_ACCOUNTS, APPLICATION_LIST_ACCOUNT_JSON)),
      new Transition(NEW_ACCOUNT, accountList, RelLink.put(REL_NEW, APPLICATION_NAME_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(GET_ACCOUNT, accountList, RelLink.get(REL_ITEM, APPLICATION_ACCOUNT_JSON)),
      new Transition(DELETE_ACCOUNT, account, RelLink.delete(REL_DELETE, APPLICATION_ACCOUNT_JSON)),
      new Transition(MODIFY_ACCOUNT_NAME, account, RelLink.put(REL_CHANGE_NAME, APPLICATION_NAME_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(MODIFY_ACCOUNT_NAME_2, account, RelLink.put(REL_CHANGE_NAME, APPLICATION_CHANGE_NAME_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(DEPOSIT, account, RelLink.put(DEPOSIT, APPLICATION_DEPOSIT_JSON, APPLICATION_ACCOUNT_JSON)),
      new Transition(WITHDRAW, account, RelLink.put(WITHDRAW, APPLICATION_WITHDRAW_JSON, APPLICATION_ACCOUNT_JSON))
    );
  }

  protected void buildHandlers(final RestServer<RoutingContext, Authorization> restServer) {
    restServer.buildHandler(LIST_ACCOUNTS, "/auth/account", permissionUser, this::handleListAccountsHead, this::handleListAccounts);
    restServer.buildHandler(NEW_ACCOUNT, "/auth/account/new", permissionUser, this::handleAccountHead, this::handleNewAccount);
    restServer.buildHandler(GET_ACCOUNT, "/auth/account", permissionUser, this::handleAccountHead, this::handleGetAccount);
    restServer.buildHandler(DELETE_ACCOUNT, "/auth/account", permissionAdmin, this::handleAccountHead, this::handleDeleteAccount);
    restServer.buildHandler(MODIFY_ACCOUNT_NAME, "/auth/account", permissionAdmin, this::handleAccountHead, this::handleModifyAccountName);
    restServer.buildHandler(MODIFY_ACCOUNT_NAME_2, "/auth/account", permissionAdmin, this::handleAccountHead, this::handleModifyAccountName2);
    restServer.buildHandler(DEPOSIT, "/auth/account", permissionUser, this::handleAccountHead, this::handleDeposit);
    restServer.buildHandler(WITHDRAW, "/auth/account", permissionUser, this::handleAccountHead, this::handleWithdraw);
  }

  private void handleListAccountsHead(final RequestContext<RoutingContext> requestContext) {
    final String number = accountManager.makeNewAccountNumber().value();
    requestContext.addQueryParameter(REL_NEW, "new", "true");
    requestContext.addQueryParameter(REL_NEW, "accountNumber", number);
  }

  private void handleListAccounts(final RequestContext<RoutingContext> requestContext) {
    final List<Account> accounts = accountManager.findAll();
    requestContext.getContext().json(accounts);
  }

  private void handleAccountHead(final RequestContext<RoutingContext> requestContext) {
    final String number = requestContext.getContext().queryParam("accountNumber").get(0);
    requestContext.addQueryParameter(REL_DELETE, "accountNumber", number);
    requestContext.addQueryParameter(REL_CHANGE_NAME, "accountNumber", number);
    requestContext.addQueryParameter(REL_DEPOSIT, "accountNumber", number);
    requestContext.addQueryParameter(REL_WITHDRAW, "accountNumber", number);
  }

  private void handleNewAccount(final RequestContext<RoutingContext> requestContext) {
    final RoutingContext context = requestContext.getContext();
    if (context.queryParam("new").isEmpty()) {
      context.next();
      return;
    }
    restServer.parseBodyAs(requestContext, Name.class).ifPresent(name -> {
      final String number = context.queryParam("accountNumber").get(0);
      final AccountNumber accountNumber = new AccountNumber(number);
      final Account addedAccount = accountManager.addAccount(accountNumber, name);
      final HttpServerResponse response = context.response();
      if (addedAccount.name().equals(name)) {
        response.setStatusCode(201);
        context.json(addedAccount);
      } else {
        response.setStatusCode(409).end("Conflict");
      }
    });
  }

  private void handleGetAccount(final RequestContext<RoutingContext> requestContext) {
    final String number = requestContext.getContext().queryParam("accountNumber").get(0);
    final AccountNumber accountNumber = new AccountNumber(number);
    final Optional<Account> accountOptional = accountManager.getAccount(accountNumber);
    if (accountOptional.isPresent()) {
      final Account account = accountOptional.get();
      requestContext.getContext().json(account);
    } else {
      final HttpServerResponse response = requestContext.getContext().response();
      response.setStatusCode(404).end("Not found");
    }
  }

  private void handleDeleteAccount(final RequestContext<RoutingContext> requestContext) {
    final String number = requestContext.getContext().queryParam("accountNumber").get(0);
    final AccountNumber accountNumber = new AccountNumber(number);
    final Optional<Account> accountOptional = accountManager.deleteAccount(accountNumber);
    if (accountOptional.isPresent()) {
      final Account account = accountOptional.get();
      requestContext.getContext().json(account);
    } else {
      final HttpServerResponse response = requestContext.getContext().response();
      response.setStatusCode(404).end("Not found");
    }
  }

  private void handleModifyAccountName(final RequestContext<RoutingContext> requestContext) {
    restServer.parseBodyAs(requestContext, Name.class).ifPresent(name -> {
      final String number = requestContext.getContext().queryParam("accountNumber").get(0);
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.modifyAccountName(accountNumber, name);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        requestContext.getContext().json(account);
      } else {
        final HttpServerResponse response = requestContext.getContext().response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }

  private void handleModifyAccountName2(final RequestContext<RoutingContext> requestContext) {
    restServer.parseBodyAs(requestContext, ChangeName.class).ifPresent(changeName -> {
      final String number = requestContext.getContext().queryParam("accountNumber").get(0);
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.modifyAccountName2(accountNumber, changeName);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        requestContext.getContext().json(account);
      } else {
        final HttpServerResponse response = requestContext.getContext().response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }

  private void handleDeposit(final RequestContext<RoutingContext> requestContext) {
    restServer.parseBodyAs(requestContext, Deposit.class).ifPresent(deposit -> {
      final String number = requestContext.getContext().queryParam("accountNumber").get(0);
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.deposit(accountNumber, deposit);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        requestContext.getContext().json(account);
      } else {
        final HttpServerResponse response = requestContext.getContext().response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }

  private void handleWithdraw(final RequestContext<RoutingContext> requestContext) {
    restServer.parseBodyAs(requestContext, Withdraw.class).ifPresent(withdraw -> {
      final String number = requestContext.getContext().queryParam("accountNumber").get(0);
      final AccountNumber accountNumber = new AccountNumber(number);
      final Optional<Account> accountOptional =
        accountManager.withdraw(accountNumber, withdraw);
      if (accountOptional.isPresent()) {
        final Account account = accountOptional.get();
        requestContext.getContext().json(account);
      } else {
        final HttpServerResponse response = requestContext.getContext().response();
        response.setStatusCode(404).end("Not found");
      }
    });
  }
}
