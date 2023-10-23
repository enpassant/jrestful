package jrestful.server;

import jrestful.RestApi;
import jrestful.Root;
import jrestful.Transition;
import jrestful.link.RelLink;
import jrestful.type.MediaType;
import jrestful.type.TypeObject;

import java.util.function.Consumer;

public interface RestServer {
  String API = "api";

  String MT_API = "application/RestAPI+json";
  MediaType api = new MediaType(
    MT_API,
    new TypeObject<>("RESTful API", "")
  );

  Transition apiTransition = new Transition(API, new Root(), RelLink.get("", MT_API));

  void init(final RestApi restApi, final Consumer<RestServer> buildHandlers);

  void buildHandler(
    final String transitionName,
    final String path,
    final RestAuthorization restAuthorization,
    final Consumer<RequestContext> process
  );

  void buildHandler(
    final String transitionName,
    final String path,
    final RestAuthorization restAuthorization,
    final Consumer<RequestContext> processHead,
    final Consumer<RequestContext> process
  );

  RestAuthorization createPermissionBased(final String permission);
}
