package co.packagemap.javaparser.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

class ASTTypeVisitor extends ASTVisitor {

  private String pkg;
  private Type clazz;

  Map<String, String> types = new HashMap<>();
  Set<String> imports = new HashSet<>();

  private record Type(String name, String modifier) {
    private Type(String name) {
      this(name, "public");
    }
  }
  ;

  public Node srcNode() {
    if (clazz == null) {
      return new Node(pkg + "." + "unknown_class_name", "public", Set.of());
    }
    return new Node(pkg + "." + clazz.name, clazz.modifier, Set.of());
  }

  public boolean visit(PackageDeclaration node) {
    this.pkg = node.getName().getFullyQualifiedName();
    return true;
  }

  // Node declaration -- generally this class
  // or a subclass defined within this class

  public boolean visit(TypeDeclaration node) {
    if (this.clazz == null) {
      this.clazz =
          new Type(node.getName().getFullyQualifiedName(), accessModifier(node.getModifiers()));
      return true;
    }

    var name = pkg + "." + clazz.name + "." + node.getName().getIdentifier();
    types.put(name, accessModifier(node.getModifiers()));
    return true;
  }

  public boolean visit(EnumDeclaration node) {
    if (this.clazz == null) {
      this.clazz =
          new Type(node.getName().getFullyQualifiedName(), accessModifier(node.getModifiers()));
      return true;
    }

    var name = pkg + "." + clazz.name + "." + node.getName().getIdentifier();
    types.put(name, accessModifier(node.getModifiers()));
    return true;
  }

  public boolean visit(RecordDeclaration node) {
    if (this.clazz == null) {
      this.clazz =
          new Type(node.getName().getFullyQualifiedName(), accessModifier(node.getModifiers()));
      return true;
    }

    var name = pkg + "." + clazz.name + "." + node.getName().getIdentifier();
    types.put(name, accessModifier(node.getModifiers()));
    return true;
  }

  // Edge declaration -- a referenced other class
  // from Variable, field, var, etc.

  public boolean visit(SimpleType node) {
    var visitor = new TypeExtractingVisitor();
    node.accept(visitor);
    visitor
        .typeString()
        .forEach(
            t -> {
              types.putIfAbsent(t, null);
            });
    return true;
  }

  public boolean visit(MethodInvocation node) {
    methodTypes(node.resolveMethodBinding()).forEach(t -> types.put(t.name, t.modifier));
    return true;
  }

  public boolean visit(MethodReference node) {
    methodTypes(node.resolveMethodBinding()).forEach(t -> types.put(t.name, t.modifier));
    return true;
  }

  private List<Type> methodTypes(IMethodBinding binding) {
    if (binding == null) {
      return List.of();
    }

    var out = new ArrayList<Type>();
    var declaringClass = binding.getDeclaringClass();

    var referencedClass =
        new Type(declaringClass.getQualifiedName(), accessModifier(declaringClass.getModifiers()));
    out.add(referencedClass);

    var methodDeclaration = binding.getMethodDeclaration();

    if (methodDeclaration.getReturnType().isPrimitive()) {
      return out;
    }

    var voidMethod = methodDeclaration.getReturnType().getName() == "void";
    if (voidMethod) {
      return out;
    }

    var returnType = methodDeclaration.getReturnType().getQualifiedName();
    var returnTypeAccess = accessModifier(methodDeclaration.getReturnType().getModifiers());

    out.add(new Type(returnType, returnTypeAccess));
    return out;
  }

  public boolean visit(ImportDeclaration node) {
    imports.add(node.getName().getFullyQualifiedName());
    if (node.isOnDemand()) {
      return true;
    }

    types.putIfAbsent(node.getName().getFullyQualifiedName(), null);
    return true;
  }

  public Set<Edge> edges() {
    var src = srcNode();

    var links =
        types.entrySet().stream()
            .map(
                entry -> {
                  var typeName = entry.getKey();
                  var typeAccess = Optional.ofNullable(entry.getValue()).orElse("public");
                  final var t = typeName;

                  var qualifiedImport =
                      imports.stream().filter(importName -> importName.endsWith("." + t)).findAny();

                  if (qualifiedImport.isPresent()) {
                    return new Edge(src, new Node(qualifiedImport.get(), typeAccess, Set.of()));
                  }

                  var pkgImports = new HashSet<>(imports);
                  pkgImports.add(pkg);
                  pkgImports.add(src.label());

                  if (typeName.contains(".") && Character.isUpperCase(typeName.charAt(0))) {
                    // Type name is prefixed with a class
                    // but not a package. i.e. WrapperClass.NestedEnum
                    var parts = typeName.split("\\.");
                    typeName = parts[parts.length - 1];

                    var pfx = String.join(".", Arrays.copyOfRange(parts, 0, parts.length - 1));

                    var alreadyImported = pkgImports.stream().anyMatch(imp -> imp.endsWith(pfx));
                    if (!alreadyImported) {
                      pkgImports.add(pkg + "." + pfx);
                    }
                  }

                  return new Edge(src, new Node(typeName, typeAccess, pkgImports));
                })
            .collect(Collectors.toSet());

    var out = new HashSet<>(links);
    out.add(new Edge(src, null));
    return out;
  }

  private String accessModifier(int modifier) {
    if ((modifier & Modifier.PUBLIC) != 0) {
      return "public";
    }

    if ((modifier & Modifier.PRIVATE) != 0) {
      return "private";
    }

    if ((modifier & Modifier.PROTECTED) != 0) {
      return "protected";
    }

    return "package_private";
  }
}
