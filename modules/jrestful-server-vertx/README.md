# jrestful-server-vertx

Vert.x Web server adapter for jrestful. It registers HTTP and HEAD routes for a
`RestApi`, handles request and response bodies, evaluates authorization, and
adds the hypermedia links available from the current representation.

The adapter can use its own Vert.x `Router` or a router managed by a framework
such as Quarkus.
