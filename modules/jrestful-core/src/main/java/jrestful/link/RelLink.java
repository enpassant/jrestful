package jrestful.link;

import jrestful.Method;
import jrestful.fp.None;
import jrestful.fp.Option;
import jrestful.fp.Some;
import jrestful.type.MediaType;

public record RelLink(
  String rel,
  Method method,
  Option<MediaType> in,
  MediaType out
) {
  public static RelLink get(
    final String rel,
    final MediaType out
  ) {
    return new RelLink(rel, Method.GET, new None(), out);
  }

  public static RelLink put(
    final String rel,
    final MediaType in,
    final MediaType out
  ) {
    return new RelLink(rel, Method.PUT, new Some(in), out);
  }

  public static RelLink delete(
    final String rel,
    final MediaType out
  ) {
    return new RelLink(rel, Method.DELETE, new None(), out);
  }

}

