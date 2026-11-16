# jrestful-server-vertx

Vert.x Web alapú szerveradapter a jrestfulhoz. HTTP- és HEAD-route-okat
regisztrál egy `RestApi` alapján, kezeli a kérés- és választörzseket, ellenőrzi
a jogosultságokat, és hozzáadja az aktuális reprezentációból elérhető
hipermédia linkeket.

Az adapter használhat saját Vert.x `Router` példányt vagy egy keretrendszer,
például a Quarkus által kezelt routert.
