package jrestful.type;

public record TypeObject<T>(String name, T content) implements Type {
  @Override
  public String toString() {
    return name + content.toString();
  }
}
