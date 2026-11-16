# jrestful Quarkus extension

The extension discovers CDI resources whose methods use `@JRestTransition`,
keeps JAX-RS `@Path`, HTTP method, `@Consumes`, `@Produces`, and
`@RolesAllowed` annotations as the server definition, and registers their
Vert.x routes during Quarkus startup.

Add the runtime extension to a Quarkus application:

```xml
<dependency>
  <groupId>io.github.enpassant</groupId>
  <artifactId>quarkus-jrestful</artifactId>
  <version>${jrestful.version}</version>
</dependency>
```

The application must provide exactly one `RestApiProvider` implementation.
Resource classes and the provider are added as CDI beans automatically, so
they do not need an explicit CDI scope.

The build fails when:

- Quarkus REST or RESTEasy is also present, because it would compete for the
  same JAX-RS annotations and routes;
- no annotated jrestful resource is found;
- the number of `RestApiProvider` implementations is not exactly one;
- an annotated handler has an unsupported signature or lacks a class-level
  `@Path` or supported HTTP method annotation.

`@RolesAllowed`, `@PermitAll`, and `@DenyAll` are evaluated by jrestful for
these transition methods. Their Quarkus CDI security interceptors are removed
at build time to avoid duplicate authorization on the raw Vert.x route; the
runtime annotations remain available to the jrestful adapter.
