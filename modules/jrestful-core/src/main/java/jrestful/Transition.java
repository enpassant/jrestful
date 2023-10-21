package jrestful;

import jrestful.link.RelLink;
import jrestful.type.MediaType;

public record Transition(String name, Context context, RelLink relLink) {
  @Override
  public String toString() {
    return "Transition{" +
      "name=" + name +
      ", context=" + context().name() +
      ", rel=" + relLink.rel() +
      ", method=" + relLink.method() +
      ", in=" + relLink.in().map(MediaType::value).orElse("") +
      ", out=" + relLink.out().value() +
      '}';
  }
}
