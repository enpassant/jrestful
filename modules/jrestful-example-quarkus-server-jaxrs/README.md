# jrestful Quarkus JAX-RS server example

This example has the same account API and authentication flow as
`jrestful-example-quarkus-server`, but declares server handlers with JAX-RS
annotations through `jrestful-server-jaxrs`.

Build the reactor dependencies first, then start Quarkus dev mode:

```shell
mvn -pl modules/jrestful-example-quarkus-server-jaxrs -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-server-jaxrs/pom.xml quarkus:dev
```

The demo users are `admin:xxx` (roles `admin,user`) and `user:xxx` (role
`user`). Basic authentication is exchanged for an RS256 Bearer token.
