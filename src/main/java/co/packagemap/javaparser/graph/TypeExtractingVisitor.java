package co.packagemap.javaparser.graph;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;

class TypeExtractingVisitor extends ASTVisitor {

  private Set<String> javaLangTypes = Set.of("String", "Integer", "Long", "Double", "Object");

  private Set<String> types = new HashSet<>();

  public boolean visit(QualifiedType node) {
    if (node.getName().toString().equals("var")) {
      return false;
    }

    if (node.isPrimitiveType()) {
      return false;
    }

    types.add(node.getName().getFullyQualifiedName());
    return false;
  }

  public boolean visit(NameQualifiedType node) {
    if (node.getName().toString().equals("var")) {
      return false;
    }

    if (node.isPrimitiveType()) {
      return false;
    }

    types.add(node.getName().getFullyQualifiedName());
    return false;
  }

  public boolean visit(SimpleType node) {
    if (node.getName().toString().equals("var")) {
      return false;
    }

    if (node.isPrimitiveType()) {
      return false;
    }

    if (!node.getName().getFullyQualifiedName().contains(".")
        && javaLangTypes.contains(node.getName().toString())) {
      return types.add("java.lang." + node.getName().toString());
    }

    types.add(node.getName().getFullyQualifiedName());
    return true;
  }

  public Set<String> typeString() {
    return types;
  }
}
