package jrestful.quarkus;

import jrestful.RestApi;
import jrestful.server.RestServer;

/** Supplies the state-transition API used by all jrestful resources in an application. */
public interface RestApiProvider {
  RestApi create(RestServer server);
}
