package io.github.enpassant.jrestful.example.quarkus.server;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class AccountServerTest {
  @Test
  void exposesTheRestApiAtTheEntryPoint() {
    given()
      .accept("application/RestAPI+json")
      .when().get("/")
      .then()
      .statusCode(200)
      .contentType("application/RestAPI+json")
      .body(containsString("listAccounts"));
  }
}
