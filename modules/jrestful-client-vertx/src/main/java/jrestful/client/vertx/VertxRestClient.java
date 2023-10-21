package jrestful.client.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import jrestful.RestApi;
import jrestful.client.ClientResult;
import jrestful.client.ClientState;
import jrestful.client.RestClient;
import jrestful.fp.None;
import jrestful.fp.Option;
import jrestful.link.Link;
import jrestful.type.MediaType;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class VertxRestClient implements RestClient {
  private static final Logger LOGGER = Logger.getLogger(VertxRestClient.class.getName());
  private final WebClient webClient;
  private Supplier<String> getToken;
  private Map<Class<?>, String> classMediaTypeMap;
  private String entryPoint;
  private RestApi restApi;
  private String token;

  public VertxRestClient(final WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public ClientResult<ClientState<RestApi>> init(
    final String entryPoint,
    final Supplier<String> getToken,
    final Map<Class<?>, String> classMediaTypeMap
  ) {
    this.entryPoint = entryPoint;
    this.getToken = getToken;
    this.classMediaTypeMap = classMediaTypeMap;
    return call(this::getMethod, "", "/", null, new None<>(), "*/*", RestApi.class);
  }

  @Override
  public ClientResult<ClientState<Void>> head(final ClientState<?> clientState, final String rel) {
    return call(this::headMethod, clientState, rel, null, Void.class);
  }

  @Override
  public <T> ClientResult<ClientState<T>> get(
    final ClientState<?> clientState,
    final String rel,
    final Class<T> responseClass
  ) {
    return call(this::getMethod, clientState, rel, null, responseClass);
  }

  @Override
  public <T> ClientResult<ClientState<T>> delete(
    final ClientState<?> clientState,
    final String rel,
    final Class<T> responseClass
  ) {
    return call(this::deleteMethod, clientState, rel, null, responseClass);
  }

  @Override
  public <T, U> ClientResult<ClientState<T>> put(
    final ClientState<?> clientState,
    final String rel,
    final U content,
    final Class<T> responseClass
  ) {
    return call(this::putMethod, clientState, rel, content, responseClass);
  }

  private HttpRequest<Buffer> getMethod(final String path) {
    return webClient.getAbs(entryPoint + path);
  }

  private HttpRequest<Buffer> headMethod(final String path) {
    return webClient.headAbs(entryPoint + path);
  }

  private HttpRequest<Buffer> putMethod(final String path) {
    return webClient.putAbs(entryPoint + path);
  }

  private HttpRequest<Buffer> deleteMethod(final String path) {
    return webClient.deleteAbs(entryPoint + path);
  }

  private <T, U> ClientResult<ClientState<T>> call(
    final Function<String, HttpRequest<Buffer>> methodFn,
    final ClientState<?> clientState,
    final String rel,
    final U content,
    final Class<T> responseClass
  ) {
    final Promise<ClientState<T>> promise = Promise.promise();
    final Optional<Link> linkOptional = clientState.getLink(rel, classMediaTypeMap);
    LOGGER.fine(() -> MessageFormat.format(
      "Call: {0}. responseClass: {1}, linkOptional: {2}",
      rel,
      responseClass,
      linkOptional
    ));

    if (linkOptional.isEmpty()) {
      final String error = MessageFormat.format("Link rel ''{0}'' not found", rel);
      promise.fail(error);
      clientState.children().add(new Throwable(error));
      return new VertxClientResult<>(promise.future());
    }
    final Link linkCall = linkOptional.get();
    final Option<String> in = linkCall.relLink().in();
    final String out = linkCall.relLink().out();
    final ClientResult<ClientState<T>> callResult = call(methodFn, rel, linkCall.path(), content, in, out, responseClass);
    callResult.onComplete(
      callState -> clientState.children().add(callState),
      throwable -> clientState.children().add(throwable)
    );
    return callResult;
  }

  private <T, U> ClientResult<ClientState<T>> call(
    final Function<String, HttpRequest<Buffer>> methodFn,
    final String rel,
    final String path,
    final U content,
    final Option<String> contentTypeOption,
    final String acceptType,
    final Class<T> responseClass
  ) {
    final Promise<ClientState<T>> promise = Promise.promise();
    final HttpRequest<Buffer> request = methodFn.apply(path)
//      .as(BodyCodec.json(responseClass))
      ;
    request.putHeader(HttpHeaders.ACCEPT.toString(), acceptType);
    if (token != null) {
      request.putHeader(HttpHeaders.AUTHORIZATION.toString(), token);
    }
    contentTypeOption.ifPresent(contentType ->
      request.putHeader(HttpHeaders.CONTENT_TYPE.toString(), contentType)
    );
    final Future<ClientState<T>> futureClientState =
      sendRequestToClient(rel, content, request, promise, responseClass);
    return new VertxClientResult<>(futureClientState);
  }

  private <T, U> Future<ClientState<T>> sendRequestToClient(
    final String rel,
    final U content,
    final HttpRequest<Buffer> request,
    final Promise<ClientState<T>> promise,
    final Class<T> responseClass
  ) {
    final Future<HttpResponse<Buffer>> httpResponseFuture = content == null ?
      request.send() :
      request.sendJson(content);
    final Future<ClientState<T>> futureClientState = httpResponseFuture
      .flatMap(response -> {
        final Buffer buffer = response.body();
        final int statusCode = response.statusCode();
        if (statusCode == 401) {
          final String header = response.getHeader("WWW-Authenticate");
          if (header != null && !header.isEmpty()) {
            final String basicToken = getToken.get();
            request.putHeader(HttpHeaders.AUTHORIZATION.toString(), basicToken);
            return sendRequestToClient(rel, content, request, promise, responseClass);
          }
        } else if (statusCode >= 300) {
          promise.fail(buffer.toString());
          return promise.future();
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        final T body;
        try {
          body = buffer == null ?
            null :
            objectMapper.readValue(buffer.getBytes(), responseClass);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
        if (body instanceof final RestApi restApi) {
          this.restApi = restApi;
        }
        final String authHeader = response.getHeader(HttpHeaders.AUTHORIZATION.toString());
        if (authHeader != null && !authHeader.isEmpty()) {
          this.token = authHeader;
        }
        if (this.restApi == null) {
          promise.fail("RestApi is missing!");
          return promise.future();
        }
        LOGGER.fine(() -> MessageFormat.format("Received response with status code: {0}", statusCode));
        final String link = response.getHeader("Link");
        LOGGER.fine(() -> MessageFormat.format("Link: {0}", link));
        final String contentType = response.getHeader("Content-Type");
        LOGGER.fine(() -> MessageFormat.format("contentType: {0}", contentType));
        final Optional<MediaType> mediaTypeOptional = restApi.getMediaType(contentType);
        LOGGER.fine(() -> MessageFormat.format("mediaTypeOptional: {0}", mediaTypeOptional));
        if (mediaTypeOptional.isPresent()) {
          final MediaType mediaType = mediaTypeOptional.get();
          final ClientState<T> nextClientState = ClientState.of(restApi, mediaType, rel, link, body);
          promise.complete(nextClientState);
          return promise.future();
        } else {
          promise.fail("Result media-type is unknown");
          return promise.future();
        }
      }).onFailure(throwable -> {
        LOGGER.info(() -> MessageFormat.format("Error: {0}", throwable));
      });
    return futureClientState;
  }
}
