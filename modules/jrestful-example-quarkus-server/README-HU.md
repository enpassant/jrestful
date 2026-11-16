# Quarkus szerverpélda

A szerver a Quarkus által kezelt Vert.x `Router` példányt használja, és erre
regisztrálja a meglévő `jrestful-server-vertx` adaptert. Nem hoz létre külön
HTTP szervert, és nem figyel külön porton.

A számlaműveletek hitelesítést igényelnek. A példában az `admin:xxx`
(`admin`, `user` szerepkörök) vagy a `user:xxx` (`user` szerepkör) használható
HTTP Basic hitelesítéssel. A válasz `Authorization` fejléce egy RS256 tokent
tartalmaz; a következő kérések ezt Bearer tokenként küldhetik. Fejlesztői és
tesztmódban a Quarkus SmallRye JWT memóriában generál kulcspárt.

Éles módban külső aláírókulcs és a hozzá tartozó publikus ellenőrzőkulcs kell:

```properties
smallrye.jwt.sign.key.location=/run/secrets/jwt-private-key.pem
mp.jwt.verify.publickey.location=/run/secrets/jwt-public-key.pem
```

A privát kulcsot nem szabad a Git repositoryba commitolni.

Fejlesztői mód indítása a repository gyökeréből:

```shell
mvn -pl modules/jrestful-example-quarkus-server -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-server/pom.xml quarkus:dev
```

Az első parancs telepíti a modul reactor-függőségeit. A második közvetlenül a
szervermodul POM-ját használja, így a Maven fel tudja oldani a `quarkus` plugin
prefixet.

Natív futtatható állomány készítése:

```shell
mvn -pl modules/jrestful-example-quarkus-server -am verify \
  -Dquarkus.native.enabled=true \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.container-runtime=podman
```
