# jrestful-server-jaxrs

Optional adapter that registers JAX-RS annotated resource methods in a jrestful
`RestServer`. It does not provide a JAX-RS runtime; the configured jrestful server
(for example `jrestful-server-vertx`) still handles HTTP traffic.

```java
@Path("/account")
class AccountResource {
  @GET
  @JRestTransition("listAccounts")
  @RolesAllowed("user")
  public void list(RequestContext context) {
    context.json(accounts.findAll());
  }

  @HEAD
  @JRestTransition("listAccounts")
  public void listHead(RequestContext context) {
    context.addQueryParameter("new", "new", "true");
  }
}

new JaxRsRestServerAdapter(restServer).register(restApi, new AccountResource());
```

Resource methods return `void` and accept one `RequestContext`. A `@HEAD` method
acts as the head processor for every transition named by its repeatable
`@JRestTransition` annotations.

Authorization uses the standard Jakarta Security annotations `@RolesAllowed`,
`@PermitAll`, and `@DenyAll`. A method-level annotation overrides a class-level
annotation. When `@RolesAllowed` contains multiple roles, any one of them is
sufficient.
