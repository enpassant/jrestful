# jrestful-server-jaxrs

Opcionális adapter, amely JAX-RS annotációkkal ellátott resource-metódusokat
regisztrál egy jrestful `RestServer` példányban. Nem biztosít JAX-RS runtime-ot;
a beállított jrestful szerver, például a `jrestful-server-vertx`, kezeli továbbra
is a HTTP-forgalmat.

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

A resource-metódusok `void` visszatérési értékűek, és egyetlen
`RequestContext` paramétert fogadnak. Egy `@HEAD` metódus minden, a megismételhető
`@JRestTransition` annotációiban megnevezett transition HEAD-feldolgozója.

A jogosultságkezelés a szabványos Jakarta Security `@RolesAllowed`,
`@PermitAll` és `@DenyAll` annotációkat használja. A metódusszintű annotáció
felülírja az osztályszintűt. Több `@RolesAllowed` szerepkör közül bármelyik
elegendő.
