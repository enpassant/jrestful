package io.github.enpassant.jrestful.example.starter;

import io.github.enpassant.jrestful.example.account.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import jrestful.RestApi;
import jrestful.client.ClientResult;
import jrestful.client.ClientState;
import jrestful.client.RestClient;
import jrestful.client.vertx.VertxRestClient;

import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class WebClientVerticle extends AbstractVerticle implements Relations {
  private static final Logger LOGGER = Logger.getLogger(WebClientVerticle.class.getName());

  private long start = System.nanoTime();

  @Override
  public void start(final Promise<Void> startPromise) {
    final ClientResult<ClientState<Account>> clientStateFuture = runLongExample()
      .andThen(result -> runSagaExample());
    clientStateFuture.onComplete(result -> startPromise.complete(), throwable -> startPromise.complete());
  }

  private ClientResult<ClientState<Account>> runLongExample() {
    final WebClient webClient = WebClient.create(vertx);

    start = System.nanoTime();

    final Map<String, Class<?>> classMediaTypeMap = new HashMap<>();

    classMediaTypeMap.put("application/Account+json", Account.class);
    classMediaTypeMap.put("application/Name+json", Name.class);
    classMediaTypeMap.put("application/ChangeName+json", ChangeName.class);
    classMediaTypeMap.put("application/Deposit+json", Deposit.class);
    classMediaTypeMap.put("application/Withdraw+json", Withdraw.class);

    final var getAdminToken = getToken("admin", "xxx");
    final var getUserToken = getToken("user", "xxx");

    final RestClient restClient = new VertxRestClient(webClient);
    final ClientResult<ClientState<RestApi>> initStateResultOTP =
      restClient.init("http://localhost:8000", getAdminToken, classMediaTypeMap);

    return initStateResultOTP.andThen(rootState ->
      restClient.head(rootState, REL_ACCOUNTS)
        .andThen(accountsState ->
          restClient.put(accountsState, REL_NEW, new Name("Teszt Elek"), Account.class)
            .andThen(newState ->
              restClient.put(newState, REL_DEPOSIT, new Deposit(new AccountNumber("12345678-87654321"), 3200.0), Account.class)
                .andThen(depositState ->
                  restClient.put(newState, REL_WITHDRAW, new Withdraw(new AccountNumber("12345678-87654321"), 1200.0), Account.class)
                )
            ).andThen(newState ->
              restClient.delete(newState, REL_DELETE, Account.class)
            )
        )
        .andThen(r ->
          restClient.head(rootState, REL_ACCOUNTS)
            .andThen(accountsState ->
              restClient.put(accountsState, REL_NEW, new Name("Paprika Piroska"), Account.class)
                .andThen(newState ->
                  restClient.put(newState, REL_DEPOSIT, new Deposit(new AccountNumber("12345678-87654321"), 9200.0), Account.class)
                    .andThen(depositState ->
                      restClient.put(newState, REL_WITHDRAW, new Withdraw(new AccountNumber("12345678-97654321"), 3400.0), Account.class)
                    )
                ).andThen(newState ->
                  restClient.put(newState, REL_CHANGE_NAME, new ChangeName(new Name("Paprika Piroska"), new Name("Chiliné Piroska")), Account.class)
                )
            )
        ).onComplete(cs -> logClientState(rootState), throwable -> logClientState(rootState))
    ).onFailure(this::handleError);
  }

  private ClientResult<ClientState<Account>> runSagaExample() {
    final WebClient webClient = WebClient.create(vertx);

    start = System.nanoTime();

    final Map<String, Class<?>> classMediaTypeMap = new HashMap<>();

    classMediaTypeMap.put("application/Account+json", Account.class);
    classMediaTypeMap.put("application/Name+json", Name.class);
    classMediaTypeMap.put("application/ChangeName+json", ChangeName.class);
    classMediaTypeMap.put("application/Deposit+json", Deposit.class);
    classMediaTypeMap.put("application/Withdraw+json", Withdraw.class);

    final var getAdminToken = getToken("admin", "xxx");
    final var getUserToken = getToken("user", "xxx");

    final RestClient restClient = new VertxRestClient(webClient);
    final ClientState<String> rootState = new ClientState<>("", List.of(), "RootState");

    final ClientResult<ClientState<RestApi>> initStateResultOTP =
      restClient.init("http://localhost:8000", getAdminToken, classMediaTypeMap);
    final ClientResult<ClientState<RestApi>> initStateResultCIB =
      restClient.init("http://localhost:8100", getAdminToken, classMediaTypeMap);

    return initStateResultOTP.join(initStateResultCIB).andThen(tuple -> {
      final ClientState<RestApi> rootStateOTP = tuple.getFirst();
      final ClientState<RestApi> rootStateCIB = tuple.getSecond();

      rootState.children().add(rootStateOTP);
      rootState.children().add(rootStateCIB);

      final double amount = 3200.0;

      return restClient.head(rootStateOTP, REL_ACCOUNTS)
        .andThen(accountsStateOTP ->
          restClient.head(rootStateCIB, REL_ACCOUNTS)
            .andThen(accountsStateCIB ->
              restClient.put(accountsStateOTP, REL_NEW, new Name("Teszt Elek"), Account.class)
                .andThen(newStateOTP -> {
                  final Account accountOTP = newStateOTP.getData();
                  return restClient.put(accountsStateCIB, REL_NEW, new Name("Paprika Piroska"), Account.class)
                    .andThen(newStateCIB -> {
                      final Account accountCIB = newStateCIB.getData();
                      return restClient.put(newStateOTP, REL_DEPOSIT, new Deposit(accountCIB.accountNumber(), amount), Account.class)
                        .andThen(depositStateOTP ->
                          restClient.put(newStateCIB, REL_WITHDRAW, new Withdraw(accountOTP.accountNumber(), amount), Account.class)
                        ).recover(throwable ->
                          restClient.put(newStateOTP, REL_WITHDRAW, new Withdraw(accountCIB.accountNumber(), amount), Account.class)
                        );
                    })
                    .onSuccess(newStateCIB -> logClientState(rootState));
                })
            )
        );
    });
  }

  private <T> void logClientState(final ClientState<T> clientState) {
    LOGGER.info(() -> MessageFormat.format("Result is succeed with: {0}", clientState));
    final long end = System.nanoTime();
    LOGGER.info(() ->
      MessageFormat.format("Running time: {0} ms", (end - start) / 1_000_000)
    );
  }

  private void handleError(final Throwable throwable) {
    LOGGER.severe(() ->
      MessageFormat.format("Something went wrong: {0}", throwable.getMessage())
    );
    throwable.printStackTrace();
    final long end = System.nanoTime();
    LOGGER.info(() ->
      MessageFormat.format("Running time: {0} ms", (end - start) / 1_000_000)
    );
  }

  private Supplier<String> getToken(final String userName, final String password) {
    final String basicToken = Base64.getEncoder()
      .encodeToString((userName + ":" + password).getBytes());
    return () -> "Basic " + basicToken;
  }
}
