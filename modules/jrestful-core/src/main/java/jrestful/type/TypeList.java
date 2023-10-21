package jrestful.type;

public record TypeList<T>(Type type) implements Type {
  public String name() {
    return toString();
  }

  @Override
  public String toString() {
    return "List<" + type + '>';
  }
}
