package jrestful.type;

import jrestful.Context;

public record MediaType(String value, Type structure) implements Type, Context {
  @Override
  public String name() {
    return value;
  }

  public String prettyPrint(final int tab) {
    final String indent = " ".repeat(tab);
    return indent + "MediaType[" +
      "\n  " + indent + value + "," +
      "\n  " + indent + structure +
      "\n" + indent + "]";
  }

  public String toString() {
    return "MediaType[" + value + ']';
  }
}
