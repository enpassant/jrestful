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

  public String toWebLink(final String queryString) {
    return path + queryString +
      "; rel=\"" + relLink.rel() +
      "\"; method=\"" + relLink.method().name() +
      "\"; type=\"" + relLink.in().orElse("") +
      "\"; accept=\"" + relLink.out() +
      "\"";
  }
}
