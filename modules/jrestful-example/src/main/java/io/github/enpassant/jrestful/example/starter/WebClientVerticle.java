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
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class WebClientVerticle extends AbstractVerticle {
  private static final Logger LOGGER = Logger.getLogger(WebClientVerticle.class.getName());

  private long start = System.nanoTime();

  @Override
  public void start(final Promise<Void> startPromise) {
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
    final ClientResult<ClientState<RestApi>> initStateResult =
      restClient.init("http://localhost:8000", getAdminToken, classMediaTypeMap);

    initStateResult.andThen(rootState ->
      restClient.head(rootState, "accounts")
        .andThen(accountsState ->
          restClient.put(accountsState, "new", new Name("Teszt Elek"), Account.class)
            .andThen(newState ->
              restClient.put(newState, "edit", new Deposit(new AccountNumber("12345678-87654321"), 3200.0), Account.class)
                .andThen(depositState ->
                  restClient.put(newState, "edit", new Withdraw(new AccountNumber("12345678-87654321"), 1200.0), Account.class)
                )
            ).andThen(newState ->
              restClient.delete(newState, "delete", Account.class)
            )
        )
        .andThen(r ->
          restClient.head(rootState, "accounts")
            .andThen(accountsState ->
              restClient.put(accountsState, "new", new Name("Paprika Piroska"), Account.class)
                .andThen(newState ->
                  restClient.put(newState, "edit", new Deposit(new AccountNumber("12345678-87654321"), 9200.0), Account.class)
                    .andThen(depositState ->
                      restClient.put(newState, "edit", new Withdraw(new AccountNumber("12345678-97654321"), 3400.0), Account.class)
                    )
                ).andThen(newState ->
                  restClient.put(newState, "edit", new ChangeName(new Name("Paprika Piroska"), new Name("Chiliné Piroska")), Account.class)
                )
            )
        ).onComplete(cs -> logClientState(rootState), throwable -> logClientState(rootState))
    ).onFailure(this::handleError);
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
