package jrestful.server;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public abstract class RequestContext {
  private final Map<String, List<String>> queryParameterMap = new LinkedHashMap<>();

  public void addQueryParameter(final String rel, final String key, final String value) {
    queryParameterMap.putIfAbsent(rel, new ArrayList<>());
    final List<String> list = queryParameterMap.get(rel);
    list.add(key + "=" + URLEncoder.encode(value));
  }

  public String qetQueryString(final String rel) {
    if (queryParameterMap.isEmpty()) {
      return "";
    } else {
      final String queryString = queryParameterMap.getOrDefault(rel, List.of()).stream()
        .collect(Collectors.joining("&"));
      return queryString.isBlank() ? "" : "?" + queryString;
    }
  }

  public abstract <T> Optional<T> parseBodyAs(final Class<T> clazz);

  public abstract void next();

  public abstract <T> CompletionStage<Void> json(final T object);

  public abstract <T> CompletionStage<Void> json(final int code, final T object);

  public abstract List<String> queryParams(final String key);

  public abstract CompletionStage<Void> sendTextWithCode(final int code, final String message);

  public String queryParam(final String key) {
    final List<String> list = queryParams(key);
    if (list.size() > 1) {
      sendTextWithCode(400, "Too many '" + key + "'query parameter!");
      return "";
    }
    if (list.isEmpty()) {
      sendTextWithCode(400, "Missing '" + key + "'query parameter!");
      return "";
    }
    return list.get(0);
  }
}
