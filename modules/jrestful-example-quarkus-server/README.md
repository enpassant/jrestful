# Quarkus server example

The server uses the Quarkus-managed Vert.x `Router` and registers the existing
`jrestful-server-vertx` adapter on it. It does not create or listen on a separate HTTP server.

Account operations require authentication. For this example, use either `admin:xxx`
(`admin`, `user` roles) or `user:xxx` (`user` role) with HTTP Basic authentication.
The response contains an RS256 token in the `Authorization` header; subsequent requests
can send that value as a Bearer token. Quarkus SmallRye JWT generates an in-memory key pair
in development and test modes.

Production mode requires an external signing key and its matching public verification key:

```properties
smallrye.jwt.sign.key.location=/run/secrets/jwt-private-key.pem
mp.jwt.verify.publickey.location=/run/secrets/jwt-public-key.pem
```

Do not commit the private key to the repository.

Run in development mode from the repository root:

```shell
mvn -pl modules/jrestful-example-quarkus-server -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-server/pom.xml quarkus:dev
```

The first command installs the module's reactor dependencies. The second command
uses the server module's POM directly so Maven can resolve the `quarkus` plugin prefix.

Build a native executable:

```shell
mvn -pl modules/jrestful-example-quarkus-server -am verify \
  -Dquarkus.native.enabled=true \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.container-runtime=podman
```
