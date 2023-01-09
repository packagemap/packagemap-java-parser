package co.packagemap.javaparser.graph;

import java.util.Set;

public class Node {

  private String label;
  private String element;
  private String accessModifier;
  private Set<String> possibleImports;

  private Node pkgNode;

  public Node(String label, String element, String accessModifier, Set<String> possibleImports) {
    this.label = label;
    this.element = element;
    this.accessModifier = accessModifier;
    this.possibleImports = possibleImports;
  }

  public String label() {
    return label;
  }

  public String element() {
    return element;
  }

  public String accessModifier() {
    return accessModifier;
  }

  public Set<String> possibleImports() {
    return possibleImports;
  }

  public String name() {
    return label.replaceAll("\\.", "_");
  }

  public boolean unqualified() {
    return packageNode().label().isEmpty();
  }

  public String unqualifiedName() {
    return label.replaceAll(packageNode().label() + "\\.", "");
  }

  public Node packageNode() {
    if (pkgNode != null) {
      return pkgNode;
    }

    char dot = '.';

    var lastUpperCase = -1;
    for (int i = 0; i < label.length(); i++) {
      char c = label.charAt(i);
      Character beforeC = null;

      if ((i - 1) >= 0) {
        beforeC = label.charAt(i - 1);
      }

      if (Character.isUpperCase(c) && (beforeC == null || beforeC.equals(dot))) {
        lastUpperCase = i;
      }
    }

    if (lastUpperCase <= 0) {
      return new Node("", "", "", Set.of());
    }

    if (label.substring(0, lastUpperCase).endsWith(".")) {
      lastUpperCase -= 1;
    }

    var pkg = label.substring(0, lastUpperCase);
    if (!pkg.isEmpty() && Character.isUpperCase(pkg.charAt(0))) {
      pkg = "";
    }

    pkgNode = new Node(pkg, "", "", Set.of());
    return pkgNode;
  }

  public boolean qualifiedBy(Node node) {
    if (!unqualified()) {
      return false;
    }

    var importMatch = possibleImports.contains(node.packageNode().label());

    if (!importMatch) {
      return false;
    }

    var nameMatch = unqualifiedName().equals(node.unqualifiedName());
    if (!nameMatch) {
      return false;
    }

    var elementMatch = element().equals(node.element());

    return elementMatch;
  }

  public boolean hasPrefix(String pkg) {
    if (pkg.isEmpty()) {
      return packageNode().label().isEmpty();
    }

    return label.startsWith(pkg);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accessModifier == null) ? 0 : accessModifier.hashCode());
    result = prime * result + ((element == null) ? 0 : element.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((pkgNode == null) ? 0 : pkgNode.hashCode());
    result = prime * result + ((possibleImports == null) ? 0 : possibleImports.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Node other = (Node) obj;
    if (accessModifier == null) {
      if (other.accessModifier != null) return false;
    } else if (!accessModifier.equals(other.accessModifier)) return false;
    if (element == null) {
      if (other.element != null) return false;
    } else if (!element.equals(other.element)) return false;
    if (label == null) {
      if (other.label != null) return false;
    } else if (!label.equals(other.label)) return false;
    if (pkgNode == null) {
      if (other.pkgNode != null) return false;
    } else if (!pkgNode.equals(other.pkgNode)) return false;
    if (possibleImports == null) {
      if (other.possibleImports != null) return false;
    } else if (!possibleImports.equals(other.possibleImports)) return false;
    return true;
  }
}
