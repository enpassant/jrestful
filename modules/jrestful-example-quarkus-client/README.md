# Quarkus client example

The client uses the existing `jrestful-client-vertx` adapter with the Quarkus-managed Vert.x instance.
It listens on port `8081` and expects the server example at `http://localhost:8080`.

With both applications running, create an account through the client application:

```shell
curl -X POST http://localhost:8081/demo/accounts \
  -H 'Content-Type: application/json' \
  -d '"Teszt Elek"'
```

The OTP server entry point is configured by `account-api.otp-url` (port `8080` by default).
The saga uses the CIB server configured by `account-api.cib-url` (port `8090` by default).

Run the complete single-server example:

```shell
curl -X POST http://localhost:8081/demo/accounts/examples/long
```

Run the two-server saga example:

```shell
curl -X POST http://localhost:8081/demo/accounts/examples/saga
```

The saga creates an account in OTP and another in CIB, applies the first operation, and compensates it if
the second operation fails. Its response explicitly reports whether compensation was performed.

Run in development mode from the repository root:

```shell
mvn -pl modules/jrestful-example-quarkus-client -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-client/pom.xml quarkus:dev
```

The first command installs the module's reactor dependencies. The second command
uses the client module's POM directly so Maven can resolve the `quarkus` plugin prefix.

Build a native executable:

```shell
mvn -pl modules/jrestful-example-quarkus-client -am package \
  -Dquarkus.native.enabled=true \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.container-runtime=podman
```
