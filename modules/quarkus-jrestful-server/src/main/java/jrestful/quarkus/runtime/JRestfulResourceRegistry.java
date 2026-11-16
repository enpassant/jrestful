package jrestful.quarkus.runtime;

import java.util.List;

public final class JRestfulResourceRegistry {
  private final List<String> resourceClassNames;

  public JRestfulResourceRegistry(final List<String> resourceClassNames) {
    this.resourceClassNames = List.copyOf(resourceClassNames);
  }

  public List<String> resourceClassNames() {
    return resourceClassNames;
  }
}
