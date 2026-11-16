package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import jrestful.RestApi;
import jrestful.quarkus.RestApiProvider;
import jrestful.server.RestServer;

public final class AccountApiProvider implements RestApiProvider {
  @Override
  public RestApi create(final RestServer server) {
    return AccountApiFactory.create(server);
  }
}
