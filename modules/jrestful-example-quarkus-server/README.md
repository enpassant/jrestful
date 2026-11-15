# Quarkus server example

The server uses the Quarkus-managed Vert.x `Router` and registers the existing
`jrestful-server-vertx` adapter on it. It does not create or listen on a separate HTTP server.

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
