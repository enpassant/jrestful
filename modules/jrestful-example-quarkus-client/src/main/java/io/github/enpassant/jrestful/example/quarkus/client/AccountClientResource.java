package io.github.enpassant.jrestful.example.quarkus.client;

import io.github.enpassant.jrestful.example.quarkus.api.Account;
import io.github.enpassant.jrestful.example.quarkus.api.Name;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.concurrent.CompletionStage;

@Path("/demo/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountClientResource {
  private final AccountClient accountClient;

  public AccountClientResource(final AccountClient accountClient) {
    this.accountClient = accountClient;
  }

  @POST
  public CompletionStage<Account> create(final Name name) {
    return accountClient.create(name);
  }

  @POST
  @Path("/examples/long")
  public CompletionStage<LongExampleResult> runLongExample() {
    return accountClient.runLongExample();
  }

  @POST
  @Path("/examples/saga")
  public CompletionStage<SagaExampleResult> runSagaExample() {
    return accountClient.runSagaExample();
  }
}
