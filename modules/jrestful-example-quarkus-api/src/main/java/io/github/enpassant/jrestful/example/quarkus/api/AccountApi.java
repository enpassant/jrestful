package io.github.enpassant.jrestful.example.quarkus.api;

public final class AccountApi {
  public static final String APPLICATION_ACCOUNT_JSON = "application/Account+json";
  public static final String APPLICATION_LIST_ACCOUNT_JSON = "application/List[Account]+json";
  public static final String APPLICATION_NAME_JSON = "application/Name+json";
  public static final String APPLICATION_CHANGE_NAME_JSON = "application/ChangeName+json";
  public static final String APPLICATION_DEPOSIT_JSON = "application/Deposit+json";
  public static final String APPLICATION_WITHDRAW_JSON = "application/Withdraw+json";

  public static final String REL_ACCOUNTS = "accounts";
  public static final String REL_NEW = "new";
  public static final String REL_ITEM = "item";
  public static final String REL_DELETE = "delete";
  public static final String REL_CHANGE_NAME = "changeName";
  public static final String REL_DEPOSIT = "deposit";
  public static final String REL_WITHDRAW = "withdraw";

  private AccountApi() {
  }
}
