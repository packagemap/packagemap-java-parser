package co.packagemap.javaparser.graph;

import java.util.List;

public record Edge(Node src, Node dst) {

  public boolean linked() {
    return src != null && dst != null && !src.label().isEmpty() && !dst().label().isEmpty();
  }

  public List<Node> nodes() {
    if (dst == null) {
      return List.of(src);
    }

    return List.of(src, dst);
  }

  public boolean packageMatch() {
    return src.packageNode().equals(dst.packageNode());
  }

  public boolean hasPrefix(String pkg) {
    return src.hasPrefix(pkg) || (linked() && dst.hasPrefix(pkg));
  }

  public boolean base(String pkg) {
    if (pkg == null || pkg.isBlank()) {
      return true;
    }

    if (linked()) {
      return src.hasPrefix(pkg) && dst.hasPrefix(pkg);
    }

    return src.hasPrefix(pkg);
  }

  public String sortKey() {
    var sb = new StringBuilder();
    sb.append(src().label());

    if (linked()) {
      sb.append(dst().label());
    }

    return sb.toString();
  }

  public String prettyPrint() {
    var sb = new StringBuilder();
    sb.append(String.format("[%s] ->", src.label()));
    if (linked()) {
      sb.append(String.format(" [%s]", dst.label()));
    }

    return sb.toString();
  }
}
