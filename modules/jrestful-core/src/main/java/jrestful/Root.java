package jrestful;

public record Root() implements Context {
  @Override
  public String name() {
    return "root";
  }

  @Override
  public String toString() {
    return "root";
  }
}
