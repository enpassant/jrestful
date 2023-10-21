package jrestful.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Types {
  private final Map<String, Type> mediaTypeMap = new HashMap<>();

  public Types(final Type... types) {
    setMediaTypes(types);
  }

  public Types() {
  }

  public Type[] getMediaTypes() {
    return mediaTypeMap.values().toArray(new Type[0]);
  }

  public void setMediaTypes(final Type... types) {
    Arrays.stream(types).forEach(
      type -> mediaTypeMap.put(type.name(), type)
    );
  }

  @Override
  public String toString() {
    return prettyPrint(0);
  }

  public String prettyPrint(final int tab) {
    final String indent = " ".repeat(tab);

    final String mediaTypeStr = mediaTypeMap.values().stream()
      .filter(type -> type instanceof MediaType)
      .map(MediaType.class::cast)
      .map(mediaType -> mediaType.prettyPrint(tab + 2))
      .sorted()
      .collect(Collectors.joining(",\n"));

    final String otherTypeStr = mediaTypeMap.values().stream()
      .filter(type -> !(type instanceof MediaType))
      .map(Object::toString)
      .sorted()
      .collect(Collectors.joining(",\n  " + indent));

    return indent + "MediaTypes:\n" + mediaTypeStr +
      "\n" + indent + "Other types:\n  " + indent + otherTypeStr;
  }
}
