# jrestful-client-vertx

Vert.x Web Client adapter for jrestful. It loads the server-provided API and
follows response links by relation and media type instead of depending on a
hard-coded list of application routes.

The adapter is usable from plain Vert.x and from Quarkus applications. There is
currently no separate jrestful Quarkus client extension.
