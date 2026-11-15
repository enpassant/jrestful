package jrestful.server.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.annotation.security.RolesAllowed;
import jrestful.RestApi;
import jrestful.Root;
import jrestful.Transition;
import jrestful.link.RelLink;
import jrestful.server.RequestContext;
import jrestful.server.RestAuthorization;
import jrestful.server.RestServer;
import jrestful.type.Types;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JaxRsRestServerAdapterTest {
  @Test
  void registersJaxRsMethodAndItsHeadProcessor() {
    final RecordingServer server = new RecordingServer();
    final RestApi api = api();

    new JaxRsRestServerAdapter(server,
      roles -> new NamedAuthorization(String.join(",", roles))).register(api, new Resource());

    final Registration registration = server.registrations.get("list");
    assertNotNull(registration);
    assertEquals("/items/all", registration.path());
    assertEquals("reader", registration.permission());
    assertNotNull(registration.head());
  }

  @Test
  void rejectsAnHttpMethodThatDoesNotMatchTheTransition() {
    final RecordingServer server = new RecordingServer();
    assertThrows(IllegalArgumentException.class,
      () -> new JaxRsRestServerAdapter(server).register(api(), new InvalidResource()));
  }

  private RestApi api() {
    return new RestApi(new Types(RecordingServer.api),
      RecordingServer.apiTransition,
      new Transition("list", new Root(), RelLink.get("items", RecordingServer.MT_API)));
  }

  @Path("/items")
  private static final class Resource {
    @GET
    @Path("all")
    @JRestTransition("list")
    @RolesAllowed("reader")
    public void list(final RequestContext context) {
    }

    @HEAD
    @JRestTransition("list")
    public void listHead(final RequestContext context) {
    }
  }

  @Path("/items")
  private static final class InvalidResource {
    @jakarta.ws.rs.DELETE
    @JRestTransition("list")
    public void list(final RequestContext context) {
    }
  }

  private record Registration(
    String path,
    String permission,
    Consumer<RequestContext> head,
    Consumer<RequestContext> process
  ) {
  }

  private static final class RecordingServer implements RestServer {
    private final Map<String, Registration> registrations = new HashMap<>();

    @Override
    public void init(final RestApi restApi, final Consumer<RestServer> buildHandlers) {
      buildHandlers.accept(this);
    }

    @Override
    public void buildHandler(final String transitionName, final String path,
                             final RestAuthorization authorization,
                             final Consumer<RequestContext> process) {
      registrations.put(transitionName, new Registration(path, permission(authorization), null, process));
    }

    @Override
    public void buildHandler(final String transitionName, final String path,
                             final RestAuthorization authorization,
                             final Consumer<RequestContext> processHead,
                             final Consumer<RequestContext> process) {
      registrations.put(transitionName,
        new Registration(path, permission(authorization), processHead, process));
    }

    @Override
    public RestAuthorization createPermissionBased(final String permission) {
      return new NamedAuthorization(permission);
    }

    private String permission(final RestAuthorization authorization) {
      return authorization instanceof NamedAuthorization named ? named.name : "all";
    }
  }

  private static final class NamedAuthorization implements RestAuthorization {
    private final String name;

    private NamedAuthorization(final String name) {
      this.name = name;
    }

    @Override
    public boolean match(final RequestContext context) {
      return true;
    }
  }
}
