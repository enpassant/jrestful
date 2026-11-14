package io.github.enpassant.jrestful.example.quarkus.api;

public final class AccountApi {
  public static final String ACCOUNT = "application/Account+json";
  public static final String ACCOUNT_LIST = "application/List[Account]+json";
  public static final String NAME = "application/Name+json";
  public static final String CHANGE_NAME = "application/ChangeName+json";
  public static final String DEPOSIT = "application/Deposit+json";
  public static final String WITHDRAW = "application/Withdraw+json";

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
