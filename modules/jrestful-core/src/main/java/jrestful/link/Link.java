package jrestful.link;

import jrestful.type.MediaType;

public record Link(String path, RelLink relLink) {
  public String toWebLink() {
    return path +
      "; rel=\"" + relLink.rel() +
      "\"; method=\"" + relLink.method().name() +
      "\"; type=\"" + relLink.in().map(MediaType::name).orElse("") +
      "\"; accept=\"" + relLink.out().name() +
      "\"";
  }
}
