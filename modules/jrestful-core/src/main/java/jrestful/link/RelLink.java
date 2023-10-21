package jrestful.link;

import jrestful.Method;
import jrestful.fp.None;
import jrestful.fp.Option;
import jrestful.fp.Some;

public record RelLink(
  String rel,
  Method method,
  Option<String> in,
  String out
) {
  public static RelLink get(
    final String rel,
    final String out
  ) {
    return new RelLink(rel, Method.GET, new None(), out);
  }

  public static RelLink put(
    final String rel,
    final String in,
    final String out
  ) {
    return new RelLink(rel, Method.PUT, new Some(in), out);
  }

  public static RelLink delete(
    final String rel,
    final String out
  ) {
    return new RelLink(rel, Method.DELETE, new None(), out);
  }

}

