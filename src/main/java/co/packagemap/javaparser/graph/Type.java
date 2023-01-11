package co.packagemap.javaparser.graph;

public record Type(String name, String modifier) {
  public Type(String name) {
    this(name, "public");
  }

  public String name() {
    return name;
  }

  public String modifier() {
    return modifier;
  }
}
