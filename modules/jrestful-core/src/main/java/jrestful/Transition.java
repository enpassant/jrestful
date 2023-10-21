package jrestful;

import jrestful.link.RelLink;

public record Transition(String name, Context context, RelLink relLink) {
  @Override
  public String toString() {
    return "Transition{" +
      "name=" + name +
      ", context=" + context().name() +
      ", rel=" + relLink.rel() +
      ", method=" + relLink.method() +
      ", in=" + relLink.in().orElse("") +
      ", out=" + relLink.out() +
      '}';
  }
}
