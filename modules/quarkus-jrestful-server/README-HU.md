# jrestful Quarkus szerverextension

Az extension felderíti azokat a CDI resource-okat, amelyek metódusai
`@JRestTransition` annotációt használnak. A JAX-RS `@Path`, HTTP metódus,
`@Consumes`, `@Produces` és `@RolesAllowed` annotációkat szerverdefinícióként
használja, majd a Quarkus indulásakor regisztrálja a Vert.x route-okat.

A runtime extension hozzáadása Quarkus alkalmazáshoz:

```xml
<dependency>
  <groupId>io.github.enpassant</groupId>
  <artifactId>quarkus-jrestful-server</artifactId>
  <version>${jrestful.version}</version>
</dependency>
```

Az alkalmazásnak pontosan egy `RestApiProvider` implementációt kell biztosítania.
A resource-osztályokat és a providert az extension automatikusan CDI beanként
regisztrálja, ezért nem szükséges explicit CDI scope.

A build hibával leáll, ha:

- a Quarkus REST vagy RESTEasy is jelen van, mert ugyanazokért a JAX-RS
  annotációkért és route-okért versenyezne;
- nem található annotált jrestful resource;
- a `RestApiProvider` implementációk száma nem pontosan egy;
- egy annotált handler aláírása nem támogatott, vagy hiányzik az osztályszintű
  `@Path`, illetve a támogatott HTTP metódus annotációja.

A transition-metódusok `@RolesAllowed`, `@PermitAll` és `@DenyAll` annotációit
a jrestful értékeli ki. A hozzájuk tartozó Quarkus CDI security interceptorokat
az extension build időben eltávolítja, hogy a nyers Vert.x route-on ne történjen
dupla jogosultság-ellenőrzés; a runtime annotációk a jrestful adapter számára
megmaradnak.
