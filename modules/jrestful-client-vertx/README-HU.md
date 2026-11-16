# jrestful-client-vertx

Vert.x Web Client alapú kliensadapter a jrestfulhoz. Betölti a szerver által
közzétett API-t, és reláció, illetve médiatípus alapján követi a válaszokban
kapott linkeket, így nem függ az alkalmazás útvonalainak előre beégetett
listájától.

Az adapter egyszerű Vert.x és Quarkus alkalmazásból is használható. Jelenleg
nincs külön jrestful Quarkus kliens-extension.
