# jrestful Quarkus JAX-RS server example

This example has the same account API and authentication flow as
`jrestful-example-quarkus-server`, but declares server handlers with JAX-RS
annotations through the `quarkus-jrestful-server` extension. The extension discovers
`AccountResource`, registers it as a CDI bean, configures JSON support and
native reflection, and validates its definition during the Quarkus build.

Build the reactor dependencies first, then start Quarkus dev mode:

```shell
mvn -pl modules/quarkus-jrestful-server-deployment,modules/jrestful-example-quarkus-server-jaxrs -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-server-jaxrs/pom.xml quarkus:dev
```

Do not add `quarkus-rest`, RESTEasy Reactive, or RESTEasy Classic to this
application. They are intentionally mutually exclusive with
`quarkus-jrestful-server`, and the build reports an error if both are present.

The demo users are `admin:xxx` (roles `admin,user`) and `user:xxx` (role
`user`). Basic authentication is exchanged for an RS256 Bearer token.
