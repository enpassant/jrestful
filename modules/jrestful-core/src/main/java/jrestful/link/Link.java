package jrestful.link;

public record Link(String path, RelLink relLink) {
  public String toWebLink() {
    return path +
      "; rel=\"" + relLink.rel() +
      "\"; method=\"" + relLink.method().name() +
      "\"; type=\"" + relLink.in().orElse("") +
      "\"; accept=\"" + relLink.out() +
      "\"";
  }
}
