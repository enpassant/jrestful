package jrestful.client;

import jrestful.Context;
import jrestful.RestApi;
import jrestful.link.Link;
import jrestful.link.RelLink;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClientState<T> {
  private final String rel;
  private final List<Link> links;
  private final T data;
  private final List<Object> children = new ArrayList<>();

  public ClientState(final String rel, final List<Link> links, final T data) {
    this.rel = rel;
    this.links = links;
    this.data = data;
  }

  public static <T> ClientState<T> of(
    final RestApi restApi,
    final Context context,
    final String rel,
    final String linkStr,
    final T data
  ) {
    final String[] linkArr = linkStr.split(",\\s*");
    final List<Link> links = Arrays.stream(linkArr)
      .map(link -> processLink(restApi, context, link))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
    return new ClientState<>(rel, links, data);
  }

  private static Optional<Link> processLink(
    final RestApi restApi,
    final Context context,
    final String linkStr
  ) {
    final String[] linkArr = linkStr.split(";\\s*");
    if (linkArr.length < 2) return Optional.empty();
    final String[] relArr = linkArr[1].split("=\\s*\"");
    final int length = relArr[1].length();
    final Optional<RelLink> link = restApi.getLink(context, relArr[1].substring(0, length - 1));
    return link.map(relLink -> new Link(linkArr[0], relLink));
  }

  public Optional<Link> getLink(final String rel, final Map<Class<?>, String> classMediaTypeMap) {
    return links.stream()
      .filter(link -> {
        final RelLink relLink = link.relLink();
        final boolean isRelEquals = relLink.rel().equalsIgnoreCase(rel);
        final boolean isOutTypeMatch = isKnownMediaType(relLink.out().name(), classMediaTypeMap);
        final Boolean isInTypeMatch = relLink.in().map(in -> isKnownMediaType(in.name(), classMediaTypeMap))
          .orElse(true);
        return isRelEquals && isOutTypeMatch && isInTypeMatch;
      })
      .findAny();
  }

  private boolean isKnownMediaType(final String mediaType, final Map<Class<?>, String> classMediaTypeMap) {
    final Pattern pattern = Pattern.compile("application/List\\[(\\w+)\\]");
    final Matcher matcher = pattern.matcher(mediaType);

    if (matcher.find()) {
      final String mtName = "application/" + matcher.group(1) + "+json";
      return classMediaTypeMap.containsValue(mtName);
    }
    return classMediaTypeMap.containsValue(mediaType);
  }

  public List<Link> links() {
    return links;
  }

  public T data() {
    return data;
  }

  public List<Object> children() {
    return children;
  }

  @Override
  public String toString() {
    return prettyPrint(0);
  }

  public String prettyPrint(final int tab) {
    final String childrenStr = children.stream()
      .map(child ->
        (child instanceof final ClientState<?> clientState) ?
          clientState.prettyPrint(tab + 2) :
          child.toString()
      )
      .collect(Collectors.joining("\n"));

    final String indent = "\n" + " ".repeat(tab);

    final String dataStr = data == null ?
      null :
      data instanceof final RestApi restApi ?
        restApi.prettyPrint(tab + 4) :
        data.toString();

    return indent + "ClientState{" +
      indent + "  rel=" + rel +
      indent + "  links=" + links +
      indent + "  data=" + dataStr +
      indent + "  children=" + childrenStr +
      indent + '}';
  }
}
