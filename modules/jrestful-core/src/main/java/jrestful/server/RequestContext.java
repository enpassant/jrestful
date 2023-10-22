package jrestful.server;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestContext<T> {
  private final T context;

  private final Map<String, List<String>> queryParameterMap = new LinkedHashMap<>();

  public RequestContext(final T context) {
    this.context = context;
  }

  public T getContext() {
    return context;
  }

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
}
