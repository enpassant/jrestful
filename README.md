# Building a Truly RESTful System

Many articles have been written about building RESTful systems. Unfortunately,
most of them do not describe RESTful APIs. They focus on URI path design,
resource naming, and choosing between POST, PUT, DELETE, or PATCH. Those are
secondary concerns and often lead us in the wrong direction.

This document presents a different approach.

## Project modules

The project is a Java 21 multi-module Maven build. Its modules fall into two
groups: reusable libraries and examples that demonstrate them.

### Library modules

- **`jrestful-core`** – REST APIs, state transitions, relations, media types,
  and shared client- and server-side abstractions.
  [README](modules/jrestful-core/README.md)
- **`jrestful-server-vertx`** – A Vert.x Web server adapter that creates HTTP
  routes and hypermedia links from `RestApi` transitions.
  [README](modules/jrestful-server-vertx/README.md)
- **`jrestful-server-jaxrs`** – An optional adapter for defining server
  resources with JAX-RS and Jakarta Security annotations. It does not provide a
  JAX-RS runtime.
  [README](modules/jrestful-server-jaxrs/README.md)
- **`jrestful-client-vertx`** – A Vert.x Web Client adapter that follows the API
  and hypermedia links received from the server.
  [README](modules/jrestful-client-vertx/README.md)
- **`quarkus-jrestful-server`** – A Quarkus server extension that discovers and
  validates JAX-RS-defined resources at build time, registers CDI beans, and
  creates Vert.x routes. There is currently no Quarkus client extension.
  [README](modules/quarkus-jrestful-server/README.md)
- **`quarkus-jrestful-server-deployment`** – The build-time half of the Quarkus
  server extension. Applications do not add it as a direct dependency.
  [Shared extension README](modules/quarkus-jrestful-server/README.md)

### Example modules

- **`jrestful-example`** – The classic Vert.x account example with client and
  server in one application. [README](modules/jrestful-example/README.md)
- **`jrestful-example-quarkus-api`** – Shared protocol vocabulary, media type
  constants, and DTOs for the Quarkus examples.
  [README](modules/jrestful-example-quarkus-api/README.md)
- **`jrestful-example-quarkus-server`** – A Quarkus server using
  `jrestful-server-vertx` directly, with Basic authentication and signed JWTs.
  [README](modules/jrestful-example-quarkus-server/README.md)
- **`jrestful-example-quarkus-server-jaxrs`** – The same API declared with
  JAX-RS annotations and served through `quarkus-jrestful-server`.
  [README](modules/jrestful-example-quarkus-server-jaxrs/README.md)
- **`jrestful-example-quarkus-client`** – A Quarkus client using
  `jrestful-client-vertx` with the Quarkus-managed Vert.x instance.
  [README](modules/jrestful-example-quarkus-client/README.md)

## Definitions of REST and RESTful

[Wikipedia's REST article](https://en.wikipedia.org/w/index.php?title=REST&oldid=1380036165)
provides the source for this section. The wording below is a concise paraphrase
of that revision; follow the link for the authoritative text and references.

REST (Representational State Transfer) is a software architectural style
created to describe the design of the World Wide Web and guide its development.
It defines constraints on the behavior of distributed, Internet-scale
hypermedia systems. Its main concerns include a uniform interface, independently
deployable components, scalable interactions, and layers that enable caching,
security enforcement, and encapsulation of legacy systems. An application that
meets the architectural constraints may be described as RESTful.

### REST constraints

- **Client/server.** A well-defined interface separates clients from servers.
- **Stateless.** An idle client consumes no server-side storage dedicated to
  that client's application state.
- **Cache.** Each response identifies whether it is cacheable.
- **Uniform interface.** Components communicate through one consistent
  interface, detailed below.
- **Layered system.** A client ordinarily cannot determine whether it is
  connected directly to the final server or through an intermediary.
- **Code on demand (optional).** A server may temporarily extend or customize a
  client by transferring logic that the client can execute in a standard
  virtual machine.

#### Uniform interface

- **Resource identification in requests.** URIs identify individual resources.
  A resource remains conceptually separate from representations such as HTML,
  XML, or JSON returned by the server.
- **Resource manipulation through representations.** A representation together
  with its metadata gives the client enough information to change or delete the
  resource state.
- **Self-descriptive messages.** A message carries enough information to choose
  how it must be processed, for example by identifying its media type.
- **Hypermedia as the engine of application state (HATEOAS).** After opening an
  initial application URI, a client discovers currently available resources by
  following links supplied dynamically by the server. The client therefore
  does not need hard-coded knowledge of the server's structure.

## Designing a REST API

When all of the constraints above are satisfied, an API is RESTful and gains
their benefits.

### Client-server architecture

Split the application into a client and a server.

### Statelessness

The common mistake is using a server-side session to store client state. Do not
do that.

### Cacheability

This is often neglected even though it matters greatly for performance. The
server should set appropriate
[cache directives](https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching),
and clients should honor them.

### Layered system

If all constraints are applied, the benefits of layering follow naturally.

### Code on demand

This optional constraint is rarely needed and is not discussed further here.

### Uniform interface

This is the most important and most debated constraint.

#### Identification of resources

Every resource must be identifiable and accessible through its identifier. In
HTTP, a URI is the natural choice. A common mistake is to identify all
resources, design routes for them, and publish that route list to clients as
the REST API.

- Identifying resources is good.
- Carefully designing their routes is unnecessary, though not inherently bad.
- Publishing the route list as the REST API is **wrong**.

The reason follows from hypermedia as the engine of application state.

##### What can be a resource?

Anything can be a resource: an object, data, a service, or even a command. More
generally, a resource is any concept that can be named.

A procedure can therefore also be a resource. Such a design may be RPC and REST
at the same time if it satisfies the REST constraints, gives the procedure an
identifier, and treats it as a resource. RPC and REST have an intersection.

#### Manipulation through representations

The definition is sufficient by itself.

#### Self-descriptive messages

The definition is sufficient by itself.

#### Hypermedia as the engine of application state

This is one of the most important requirements:

*Clients move only through states made available by hypermedia links supplied
by the server. Apart from a few entry points, they do not assume that operations
exist.*

Despite being mandatory, it is often ignored: servers omit links, clients enter
states that were not advertised, and clients assume a predefined route for
every resource. A system that behaves this way is not RESTful.

##### Why is this so important?

- Client and server can genuinely evolve independently. Features and resource
  identifiers can change without changing the client.
- Versioning is easier. Old clients continue to use old functionality, while a
  client that understands a new feature will use it only after the server
  advertises its link. Client and server releases need not be simultaneous.
- Resources may move to other servers without changing client applications.
- Errors from manually constructed paths—missing segments, wrong ordering, or
  missing URL encoding—disappear.
- Invalid transitions can be reduced. A server advertises an order's `pay` link
  only while the order is unpaid.

##### What must the client know?

The client needs to know only:

1. An application **entry point**, such as `/api`.
2. The **relations** between resource representations.
3. The contents of the **media types** it understands.

After accessing a resource—or requesting only its headers with HEAD—the client
receives related hypermedia links and the media type of each target resource.
It need not understand every relation or media type.

For example:

```text
Link: <https://.../api/orders/3f2418ca/pay>; rel="pay"; type="application/PaymentCard+json"
```

The `pay` relation identifies the purpose of the URI, while the media type says
that card details must be submitted. A client that understands both can present
and perform the payment operation.

## Additional considerations

### Resource identification

Resource identification is a major problem in conventional APIs, especially
when resources must be renamed or reorganized. Here resources may be named
freely, and hard-to-guess identifiers such as UUIDs are often preferable:

`/api/7a79169c-a5a2-4b96-86c6-c0dc3d23f2dc` versus `/api/orders/4`

- Which identifier is easier for an attacker to guess?
- Which one makes the client more likely to follow hypermedia links?

### Versioning

An incompatible change to `application/PaymentCard+json` can be introduced as
`application/PaymentCard2+json`. Clients that understand the new media type can
use it, while the old type may remain temporarily during migration.

### HTTP methods

Prefer safe methods because they do not change server state. When state must
change, prefer idempotent methods. An idempotent operation leaves the server in
the same state whether it is sent once or repeatedly.

Consider creating an order:

- **POST** `/api/orders/` creates an order and returns its assigned identifier.
- **PUT** `/api/orders/4` creates order 4 or replaces it if it exists.

PUT may appear dangerous because another client might create order 4 at the
same time. This can be handled by requiring creation only, for example with
`PUT /api/orders/4?new=true`, or by using a sufficiently unique UUID. Because
the server provides the link, the client need not know how the UUID was made.

Idempotency is also valuable when no response arrives. PUT can safely be
retried. Retrying POST may create several resources, while not retrying may
create none.

The preferred methods are:

- **GET** for retrieving resources; safe, cacheable, and idempotent.
- **PUT** for creating and modifying resources; idempotent.
- **DELETE** for deleting resources; idempotent.
- **HEAD** for retrieving headers such as links; safe, cacheable, and
  idempotent.

#### Remote procedure calls

An arbitrary procedure can be modeled as a resource. For example:

```text
launchMissile(date, target)
```

The `/api/procedures/launchMissile` resource could accept
`application/LaunchMissileParameter+json`. This can satisfy REST, although it is
not fully aligned with its spirit and should be used only when necessary.

#### Remote function calls

A function operates on an existing resource or produces a resource as output.
Several representations are possible:

1. Make it a child resource, such as `/api/orders/3f2418ca/pay`, and submit
   parameters using a media type such as `PaymentParameters`.
2. Send the function's media type, such as `Payment`, to the resource being
   modified.
3. Send the function's media type to the address of the resource being created.

### Return values

Two approaches are reasonable, with the first preferred here:

- Always use HTTP status codes such as 200 and 404.
- Use status codes for protocol-level results and represent application-level
  problems inside the returned resource. This separates protocol and
  application errors and can ease a future protocol change.

### Account management example

The example supports listing, creating, retrieving, deleting, renaming,
depositing to, and withdrawing from accounts.

Entry point (`root`): `/api`

Transitions:

| Context       | rel        | method | Content-Type media type | Accept media type |
|---------------|------------|--------|-------------------------|-------------------|
| `root`        | accounts   | GET    |                         | List[Account]     |
| List[Account] | new        | PUT    | Account                 | Account           |
| List[Account] | item       | GET    |                         | Account           |
| Account       | delete     | DELETE |                         | Account           |
| Account       | changeName | PUT    | Name                    | Account           |
| Account       | deposit    | PUT    | Deposit                 | Account           |
| Account       | withdraw   | PUT    | Withdraw                | Account           |

Media types:

- `Account(name: Name, number: AccountNumber, balance: Double)`
- `Name: String[80]`
- `Deposit(sourceAccount: AccountNumber, amount: Double)`
- `Withdraw(targetAccount: AccountNumber, amount: Double)`

Additional data type:

- `AccountNumber: [0-9]{16,24}`
