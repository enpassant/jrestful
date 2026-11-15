package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import io.github.enpassant.jrestful.example.quarkus.api.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jrestful.server.RequestContext;
import jrestful.server.jaxrs.JRestTransition;

import static io.github.enpassant.jrestful.example.quarkus.api.AccountApi.*;

@Path("/account")
public final class AccountResource {
  public static final String LIST_ACCOUNTS = "listAccounts";
  public static final String NEW_ACCOUNT = "newAccount";
  public static final String GET_ACCOUNT = "getAccount";
  public static final String DELETE_ACCOUNT = "deleteAccount";
  public static final String CHANGE_ACCOUNT_NAME = "changeAccountName";
  public static final String CHANGE_ACCOUNT_NAME_IF_CURRENT = "changeAccountNameIfCurrent";
  public static final String DEPOSIT_ACCOUNT = "depositAccount";
  public static final String WITHDRAW_ACCOUNT = "withdrawAccount";

  private final AccountManager accounts;

  public AccountResource(final AccountManager accounts) { this.accounts = accounts; }

  @HEAD
  @JRestTransition(LIST_ACCOUNTS)
  public void listHead(final RequestContext context) {
    context.addQueryParameter(REL_NEW, "new", "true");
    context.addQueryParameter(REL_NEW, "accountNumber", accounts.nextNumber().value());
  }

  @HEAD
  @JRestTransition(NEW_ACCOUNT)
  @JRestTransition(GET_ACCOUNT)
  @JRestTransition(DELETE_ACCOUNT)
  @JRestTransition(CHANGE_ACCOUNT_NAME)
  @JRestTransition(CHANGE_ACCOUNT_NAME_IF_CURRENT)
  @JRestTransition(DEPOSIT_ACCOUNT)
  @JRestTransition(WITHDRAW_ACCOUNT)
  public void accountHead(final RequestContext context) {
    final String number = context.queryParam("accountNumber");
    context.addQueryParameter(REL_DELETE, "accountNumber", number);
    context.addQueryParameter(REL_CHANGE_NAME, "accountNumber", number);
    context.addQueryParameter(REL_DEPOSIT, "accountNumber", number);
    context.addQueryParameter(REL_WITHDRAW, "accountNumber", number);
  }

  @GET
  @Produces(ACCOUNT_LIST)
  @JRestTransition(LIST_ACCOUNTS)
  @RolesAllowed("user")
  public void list(final RequestContext context) { context.json(accounts.findAll()); }

  @PUT
  @Path("/new")
  @Consumes(NAME)
  @Produces(ACCOUNT)
  @JRestTransition(NEW_ACCOUNT)
  @RolesAllowed("user")
  public void create(final RequestContext context) {
    context.parseBodyAs(Name.class).ifPresent(name -> context.json(201, accounts.add(number(context), name)));
  }

  @GET
  @Produces(ACCOUNT)
  @JRestTransition(GET_ACCOUNT)
  @RolesAllowed("user")
  public void get(final RequestContext context) {
    accounts.get(number(context)).ifPresentOrElse(context::json, () -> notFound(context));
  }

  @DELETE
  @Produces(ACCOUNT)
  @JRestTransition(DELETE_ACCOUNT)
  @RolesAllowed("admin")
  public void delete(final RequestContext context) {
    accounts.delete(number(context)).ifPresentOrElse(context::json, () -> notFound(context));
  }

  @PUT
  @Consumes(NAME)
  @Produces(ACCOUNT)
  @JRestTransition(CHANGE_ACCOUNT_NAME)
  @RolesAllowed("admin")
  public void changeName(final RequestContext context) {
    context.parseBodyAs(Name.class).ifPresent(name ->
      accounts.changeName(number(context), name).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  @PUT
  @Consumes(CHANGE_NAME)
  @Produces(ACCOUNT)
  @JRestTransition(CHANGE_ACCOUNT_NAME_IF_CURRENT)
  @RolesAllowed("admin")
  public void changeNameIfCurrent(final RequestContext context) {
    context.parseBodyAs(ChangeName.class).ifPresent(change ->
      accounts.changeName(number(context), change).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  @PUT
  @Consumes(DEPOSIT)
  @Produces(ACCOUNT)
  @JRestTransition(DEPOSIT_ACCOUNT)
  @RolesAllowed("user")
  public void deposit(final RequestContext context) {
    context.parseBodyAs(Deposit.class).ifPresent(value ->
      accounts.deposit(number(context), value).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  @PUT
  @Consumes(WITHDRAW)
  @Produces(ACCOUNT)
  @JRestTransition(WITHDRAW_ACCOUNT)
  @RolesAllowed("user")
  public void withdraw(final RequestContext context) {
    context.parseBodyAs(Withdraw.class).ifPresent(value ->
      accounts.withdraw(number(context), value).ifPresentOrElse(context::json, () -> notFound(context)));
  }

  private AccountNumber number(final RequestContext context) {
    return new AccountNumber(context.queryParam("accountNumber"));
  }
  private void notFound(final RequestContext context) { context.sendTextWithCode(404, "Account not found"); }
}
