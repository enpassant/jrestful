package jrestful.client;

import jrestful.Context;
import jrestful.Method;
import jrestful.RestApi;
import jrestful.fp.None;
import jrestful.fp.Some;
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
    return parseLink(linkStr)
      .flatMap(parsedLink ->
        restApi.getLink(context, parsedLink.relLink())
          .map(relLink -> new Link(parsedLink.path(), relLink))
      );
  }

  private static Optional<Link> parseLink(final String linkStr) {
    final Pattern pattern = Pattern.compile(
      "([^;]+);\\s*rel=\"([^;]+)\";\\s*method=\"([^;]+)\";\\s*type=\"([^;]*)\";\\s*accept=\"([^;]+)\""
    );
    final Matcher matcher = pattern.matcher(linkStr);
    if (matcher.find()) {
      final String group4 = matcher.group(4);
      return Optional.of(
        new Link(
          matcher.group(1),
          new RelLink(
            matcher.group(2),
            Method.valueOf(matcher.group(3)),
            group4.isBlank() ? new None<>() : new Some<>(group4),
            matcher.group(5)
          )
        )
      );
    } else {
      return Optional.empty();
    }
  }

  public Optional<Link> getLink(
    final String rel,
    final Class<?> inClass,
    final Class<?> outClass,
    final Map<String, Class<?>> classMediaTypeMap
  ) {
    return links.stream()
      .filter(link -> {
        final RelLink relLink = link.relLink();
        final boolean isRelEquals = relLink.rel().equalsIgnoreCase(rel);
        final boolean isOutTypeMatch = getKnownMediaType(relLink.out(), classMediaTypeMap)
          .map(clazz -> outClass.equals(Void.class) || clazz.equals(outClass))
          .orElse(false);
        final Boolean isInTypeMatch = relLink.in().toOptional().flatMap(
            in -> getKnownMediaType(in, classMediaTypeMap)
          ).map(clazz -> inClass.equals(Void.class) || clazz.equals(inClass))
          .orElse(true);
        return isRelEquals && isOutTypeMatch && isInTypeMatch;
      })
      .findAny();
  }

  private Optional<Class<?>> getKnownMediaType(final String mediaType, final Map<String, Class<?>> classMediaTypeMap) {
    final Pattern pattern = Pattern.compile("application/List\\[(\\w+)]");
    final Matcher matcher = pattern.matcher(mediaType);

    if (matcher.find()) {
      final String mtName = "application/" + matcher.group(1) + "+json";
      return Optional.ofNullable(
        classMediaTypeMap.getOrDefault(mtName, null)
      );
    }
    return Optional.ofNullable(
      classMediaTypeMap.getOrDefault(mediaType, null)
    );
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
