package jrestful.type;

public record TypeObject<T>(T content) implements Type {
  @Override
  public String toString() {
    return content.toString();
  }
}
