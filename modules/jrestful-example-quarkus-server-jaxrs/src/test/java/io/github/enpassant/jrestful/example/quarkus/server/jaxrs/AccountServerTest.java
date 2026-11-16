package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
class AccountServerTest {
  @Test
  void exposesTheRestApiAtTheEntryPoint() {
    given().accept("application/RestAPI+json").when().get("/").then()
      .statusCode(200).body(containsString("listAccounts"));
  }

  @Test
  void requiresAuthenticationForAccountOperations() {
    given().accept("application/List[Account]+json").when().get("/account").then().statusCode(401);
  }

  @Test
  void exchangesBasicAuthenticationForAnRs256BearerToken() {
    final String token = given().auth().preemptive().basic("admin", "xxx")
      .accept("application/List[Account]+json").when().get("/account").then()
      .statusCode(200).header("Authorization", startsWith("Bearer eyJ"))
      .extract().header("Authorization");
    given().header("Authorization", token).accept("application/List[Account]+json")
      .when().get("/account").then().statusCode(200);
  }

  @Test
  void listsAccountsWithABrowserStyleGenericAcceptHeader() {
    given().auth().preemptive().basic("admin", "xxx").accept("*/*")
      .when().get("/account").then().statusCode(200)
      .body(startsWith("["));
  }

  @Test
  void servesAnAnnotatedPutResourceMethod() {
    given().auth().preemptive().basic("admin", "xxx")
      .contentType("application/Name+json").accept("application/Account+json")
      .body("\"Teszt Elek\"")
      .when().put("/account/new?new=true&accountNumber=12345678-87654321")
      .then().statusCode(201)
      .body("name", equalTo("Teszt Elek"))
      .body("accountNumber", equalTo("12345678-87654321"));
  }

  @Test
  void appliesAnnotatedPermissions() {
    given().auth().preemptive().basic("user", "xxx").accept("application/Account+json")
      .when().delete("/account?accountNumber=missing").then().statusCode(403);
  }
}
