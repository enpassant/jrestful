package io.github.enpassant.jrestful.example.starter;

import io.github.enpassant.jrestful.example.account.Account;
import io.github.enpassant.jrestful.example.account.Relations;
import io.github.enpassant.jrestful.example.account.client.AccountRestClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import jrestful.client.ClientResult;
import jrestful.client.ClientState;
import jrestful.client.vertx.VertxRestClient;

import java.util.logging.Logger;

public class WebClientVerticle extends AbstractVerticle implements Relations {
  private static final Logger LOGGER = Logger.getLogger(WebClientVerticle.class.getName());

  @Override
  public void start(final Promise<Void> startPromise) {
    final WebClient webClient = WebClient.create(vertx);
    final VertxRestClient vertxRestClient = new VertxRestClient(webClient);
    final AccountRestClient accountRestClient = new AccountRestClient(vertxRestClient);

    final ClientResult<ClientState<Account>> clientStateFuture = accountRestClient.runLongExample()
      .andThen(result -> accountRestClient.runSagaExample());
    clientStateFuture.onComplete(result -> startPromise.complete(), throwable -> startPromise.complete());
  }
}
