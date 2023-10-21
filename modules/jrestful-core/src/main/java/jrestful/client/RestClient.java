package jrestful.client;

import jrestful.RestApi;

import java.util.Map;
import java.util.function.Supplier;

public interface RestClient {
  ClientResult<ClientState<RestApi>> init(
    final String entryPoint,
    final Supplier<String> getToken,
    final Map<Class<?>, String> classMediaTypeMap
  );

  ClientResult<ClientState<Void>> head(final ClientState<?> clientState, final String rel);

  <T> ClientResult<ClientState<T>> get(
    final ClientState<?> clientState,
    final String rel,
    final Class<T> responseClass
  );

  <T> ClientResult<ClientState<T>> delete(
    final ClientState<?> clientState,
    final String rel,
    final Class<T> responseClass
  );

  <T, U> ClientResult<ClientState<T>> put(
    final ClientState<?> clientState,
    final String rel,
    final U content,
    final Class<T> responseClass
  );
}
