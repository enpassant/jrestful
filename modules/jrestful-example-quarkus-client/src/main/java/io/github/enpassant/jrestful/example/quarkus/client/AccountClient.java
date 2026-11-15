package io.github.enpassant.jrestful.example.quarkus.client;

import io.github.enpassant.jrestful.example.quarkus.api.Account;
import io.github.enpassant.jrestful.example.quarkus.api.AccountNumber;
import io.github.enpassant.jrestful.example.quarkus.api.ChangeName;
import io.github.enpassant.jrestful.example.quarkus.api.Deposit;
import io.github.enpassant.jrestful.example.quarkus.api.Name;
import io.github.enpassant.jrestful.example.quarkus.api.Withdraw;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jrestful.client.ClientState;
import jrestful.client.RestClient;
import jrestful.client.vertx.VertxRestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Supplier;

import static io.github.enpassant.jrestful.example.quarkus.api.AccountApi.*;

@ApplicationScoped
public class AccountClient {
  private static final Supplier<String> ADMIN_CREDENTIALS = () -> "Basic " + Base64.getEncoder()
    .encodeToString("admin:xxx".getBytes(StandardCharsets.UTF_8));
  private final Vertx vertx;
  private final String otpEntryPoint;
  private final String cibEntryPoint;

  public AccountClient(
    final Vertx vertx,
    @ConfigProperty(name = "account-api.otp-url") final String otpEntryPoint,
    @ConfigProperty(name = "account-api.cib-url") final String cibEntryPoint
  ) {
    this.vertx = vertx;
    this.otpEntryPoint = otpEntryPoint;
    this.cibEntryPoint = cibEntryPoint;
  }

  public CompletionStage<Account> create(final Name name) {
    final RestClient client = newClient();
    final CompletableFuture<Account> result = new CompletableFuture<>();

    client.init(otpEntryPoint, ADMIN_CREDENTIALS, AccountClientTypes.KNOWN_TYPES)
      .andThen(root -> client.head(root, REL_ACCOUNTS))
      .andThen(accounts -> client.put(accounts, REL_NEW, name, Account.class))
      .onSuccess(account -> result.complete(account.getData()))
      .onFailure(result::completeExceptionally);

    return result;
  }

  public CompletionStage<LongExampleResult> runLongExample() {
    final long startedAt = System.nanoTime();
    final RestClient client = newClient();
    final CompletableFuture<LongExampleResult> result = new CompletableFuture<>();
    final AtomicReference<Account> deletedAccount = new AtomicReference<>();

    client.init(otpEntryPoint, ADMIN_CREDENTIALS, AccountClientTypes.KNOWN_TYPES)
      .andThen(root ->
        client.head(root, REL_ACCOUNTS)
          .andThen(accounts ->
            client.put(accounts, REL_NEW, new Name("Teszt Elek"), Account.class)
              .andThen(created ->
                client.put(created, REL_DEPOSIT,
                    new Deposit(new AccountNumber("12345678-87654321"), 3200.0), Account.class)
                  .andThen(deposited ->
                    client.put(deposited, REL_WITHDRAW,
                      new Withdraw(new AccountNumber("12345678-87654321"), 1200.0), Account.class))
              )
              .andThen(account -> client.delete(account, REL_DELETE, Account.class))
              .onSuccess(deleted -> deletedAccount.set(deleted.getData()))
          )
          .andThen(ignored ->
            client.head(root, REL_ACCOUNTS)
              .andThen(accounts ->
                client.put(accounts, REL_NEW, new Name("Paprika Piroska"), Account.class)
                  .andThen(created ->
                    client.put(created, REL_DEPOSIT,
                        new Deposit(new AccountNumber("12345678-87654321"), 9200.0), Account.class)
                      .andThen(deposited ->
                        client.put(deposited, REL_WITHDRAW,
                          new Withdraw(new AccountNumber("12345678-97654321"), 3400.0), Account.class))
                  )
                  .andThen(account ->
                    client.put(account, REL_CHANGE_NAME,
                        new ChangeName(new Name("Paprika Piroska"), new Name("Chiliné Piroska")), Account.class)
                      .recover(ignoredFailure ->
                        client.put(account, REL_CHANGE_NAME, new Name("Chiliné Piroska"), Account.class))
                  )
              )
          )
      )
      .onSuccess(finalAccount -> result.complete(new LongExampleResult(
        deletedAccount.get(),
        finalAccount.getData(),
        elapsedMillis(startedAt)
      )))
      .onFailure(result::completeExceptionally);

    return result;
  }

  public CompletionStage<SagaExampleResult> runSagaExample() {
    final long startedAt = System.nanoTime();
    final RestClient otpClient = newClient();
    final RestClient cibClient = newClient();
    final CompletableFuture<SagaExampleResult> result = new CompletableFuture<>();
    final AtomicReference<Account> otpAccount = new AtomicReference<>();
    final AtomicReference<Account> cibAccount = new AtomicReference<>();
    final AtomicReference<ClientState<Account>> otpState = new AtomicReference<>();
    final AtomicReference<ClientState<Account>> cibState = new AtomicReference<>();
    final AtomicBoolean compensated = new AtomicBoolean();
    final double amount = 3200.0;

    otpClient.init(otpEntryPoint, ADMIN_CREDENTIALS, AccountClientTypes.KNOWN_TYPES)
      .join(cibClient.init(cibEntryPoint, ADMIN_CREDENTIALS, AccountClientTypes.KNOWN_TYPES))
      .andThen(roots ->
        otpClient.head(roots.getFirst(), REL_ACCOUNTS)
          .join(cibClient.head(roots.getSecond(), REL_ACCOUNTS))
      )
      .andThen(accountLists ->
        otpClient.put(accountLists.getFirst(), REL_NEW, new Name("Teszt Elek"), Account.class)
          .andThen(createdOtp -> {
            otpState.set(createdOtp);
            otpAccount.set(createdOtp.getData());
            return cibClient.put(accountLists.getSecond(), REL_NEW,
              new Name("Paprika Piroska"), Account.class);
          })
          .andThen(createdCib -> {
            cibState.set(createdCib);
            cibAccount.set(createdCib.getData());
            return otpClient.put(
              otpState.get(),
              REL_DEPOSIT,
              new Deposit(cibAccount.get().accountNumber(), amount),
              Account.class
            );
          })
          .andThen(depositedOtp -> {
            otpState.set(depositedOtp);
            otpAccount.set(depositedOtp.getData());
            return cibClient.put(
                cibState.get(),
                REL_WITHDRAW,
                new Withdraw(otpAccount.get().accountNumber(), amount),
                Account.class
              )
              .onSuccess(withdrawn -> cibAccount.set(withdrawn.getData()))
              .recover(failure -> {
                compensated.set(true);
                return otpClient.put(
                  depositedOtp,
                  REL_WITHDRAW,
                  new Withdraw(cibAccount.get().accountNumber(), amount),
                  Account.class
                );
              });
          })
      )
      .onSuccess(finalState -> {
        if (compensated.get()) {
          otpAccount.set(finalState.getData());
        }
        result.complete(new SagaExampleResult(
          otpAccount.get(),
          cibAccount.get(),
          amount,
          compensated.get(),
          elapsedMillis(startedAt)
        ));
      })
      .onFailure(result::completeExceptionally);

    return result;
  }

  private RestClient newClient() {
    return new VertxRestClient(WebClient.create(vertx));
  }

  private long elapsedMillis(final long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
