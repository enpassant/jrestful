package jrestful.server;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestContext<T> {
  private final T context;

  private final Map<String, String> queryParameterMap = new LinkedHashMap<>();

  public RequestContext(final T context) {
    this.context = context;
  }

  public T getContext() {
    return context;
  }

  public void addQueryParameter(final String key, final String value) {
    queryParameterMap.put(key, value);
  }

  public String qetQueryString() {
    if (queryParameterMap.isEmpty()) {
      return "";
    } else {
      return "?" + queryParameterMap.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue()))
        .collect(Collectors.joining("&"));
    }
  }
}
