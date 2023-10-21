package jrestful.type;

public record TypeList<T>(Type type) implements Type {
  @Override
  public String toString() {
    return "List<" + type + '>';
  }
}
