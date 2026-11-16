# jrestful Quarkus JAX-RS szerverpélda

Ez a példa ugyanazt a számlakezelő API-t és hitelesítési folyamatot biztosítja,
mint a `jrestful-example-quarkus-server`, de a szerverhandlereket JAX-RS
annotációkkal, a `quarkus-jrestful-server` extensionön keresztül deklarálja.
Az extension felderíti az `AccountResource` osztályt, CDI beanként regisztrálja,
beállítja a JSON- és natív reflection támogatást, majd a Quarkus build során
ellenőrzi a definíciót.

Először építsük fel a reactor-függőségeket, majd indítsuk a Quarkus dev mode-ot:

```shell
mvn -pl modules/quarkus-jrestful-server-deployment,modules/jrestful-example-quarkus-server-jaxrs -am install -DskipTests
mvn -f modules/jrestful-example-quarkus-server-jaxrs/pom.xml quarkus:dev
```

Az alkalmazáshoz nem szabad hozzáadni a `quarkus-rest`, RESTEasy Reactive vagy
RESTEasy Classic extensiont. Ezek szándékosan kizárják egymást a
`quarkus-jrestful-server` extensionnel, és együttes jelenlétükkor a build hibát
jelez.

A felhasználók: `admin:xxx` (`admin,user` szerepkörök) és `user:xxx` (`user`
szerepkör). A Basic hitelesítést a szerver RS256 Bearer tokenre cseréli.
