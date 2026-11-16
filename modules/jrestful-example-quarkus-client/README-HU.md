# Quarkus klienspélda

A kliens a meglévő `jrestful-client-vertx` adaptert használja a Quarkus által
kezelt Vert.x példánnyal. A `8081`-es porton figyel, és a szerverpéldát a
`http://localhost:8080` címen várja.

Mindkét alkalmazás futása közben így hozható létre számla a kliensen keresztül:

```shell
curl -X POST http://localhost:8081/demo/accounts \
  -H 'Content-Type: application/json' \
  -d '"Teszt Elek"'
```

Az OTP szerver belépési pontját az `account-api.otp-url` konfigurálja,
alapértelmezetten a `8080`-as porton. A saga az `account-api.cib-url` által
megadott CIB szervert használja, alapértelmezetten a `8090`-es porton.

A teljes, egy szervert használó példa futtatása:

```shell
curl -X POST http://localhost:8081/demo/accounts/examples/long
```

A két szervert használó saga példa futtatása:

```shell
curl -X POST http://localhost:8081/demo/accounts/examples/saga
```

A saga létrehoz egy számlát az OTP, majd egy másikat a CIB rendszerben,
végrehajtja az első műveletet, és kompenzálja, ha a második sikertelen. A válasz
egyértelműen jelzi, hogy történt-e kompenzáció.

Fejlesztői mód indítása a repository gyökeréből:

```shell
mvn -pl modules/jrestful-example-quarkus-client -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-client/pom.xml quarkus:dev
```

Az első parancs telepíti a modul reactor-függőségeit. A második közvetlenül a
kliensmodul POM-ját használja, így a Maven fel tudja oldani a `quarkus` plugin
prefixet.

Natív futtatható állomány készítése:

```shell
mvn -pl modules/jrestful-example-quarkus-client -am package \
  -Dquarkus.native.enabled=true \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.container-runtime=podman
```
