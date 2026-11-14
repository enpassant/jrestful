# Quarkus server example

The server uses the Quarkus-managed Vert.x `Router` and registers the existing
`jrestful-server-vertx` adapter on it. It does not create or listen on a separate HTTP server.

Run in development mode from the repository root:

```shell
mvn -pl modules/jrestful-example-quarkus-server -am quarkus:dev
```

Build a native executable:

```shell
mvn -pl modules/jrestful-example-quarkus-server -am verify \
  -Dquarkus.native.enabled=true \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.container-runtime=podman
```
