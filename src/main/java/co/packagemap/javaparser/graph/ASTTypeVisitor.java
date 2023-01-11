package co.packagemap.javaparser.graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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

  Map<ElementType, String> types = new HashMap<>();
  Set<String> imports = new HashSet<>();
  private final Deque<Type> methodStack = new ArrayDeque<>();

  public Node srcNode(Type element) {
    if (element != null && !element.name().isEmpty()) {
      return new Node(pkg + "." + clazz.name(), element.name(), element.modifier(), Set.of());
    }

    if (clazz == null) {
      return new Node(pkg + "." + "unknown_class_name", "", "public", Set.of());
    }

    return new Node(pkg + "." + clazz.name(), "", clazz.modifier(), Set.of());
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

    var name = pkg + "." + clazz.name() + "." + node.getName().getIdentifier();
    types.put(new ElementType(new Type(""), name, ""), accessModifier(node.getModifiers()));
    return true;
  }

  public boolean visit(EnumDeclaration node) {
    if (this.clazz == null) {
      this.clazz =
          new Type(node.getName().getFullyQualifiedName(), accessModifier(node.getModifiers()));
      return true;
    }

    var name = pkg + "." + clazz.name() + "." + node.getName().getIdentifier();
    types.put(new ElementType(new Type(""), name, ""), accessModifier(node.getModifiers()));
    return true;
  }

  public boolean visit(RecordDeclaration node) {
    if (this.clazz == null) {
      this.clazz =
          new Type(node.getName().getFullyQualifiedName(), accessModifier(node.getModifiers()));
      return true;
    }

    var name = pkg + "." + clazz.name() + "." + node.getName().getIdentifier();
    types.put(new ElementType(new Type(""), name, ""), accessModifier(node.getModifiers()));
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
              types.putIfAbsent(new ElementType(new Type(""), t, ""), null);
            });
    return true;
  }

  public boolean visit(MethodDeclaration node) {
    var mod = accessModifier(node.getModifiers());
    methodStack.push(new Type(node.getName().getIdentifier(), mod));
    return true;
  }

  public void endVisit(MethodDeclaration node) {
    methodStack.pop();
  }

  public boolean visit(MethodInvocation node) {
    methodTypes(node.resolveMethodBinding())
        .entrySet()
        .forEach(entry -> types.put(entry.getKey(), entry.getValue()));
    return true;
  }

  public boolean visit(MethodReference node) {
    methodTypes(node.resolveMethodBinding())
        .entrySet()
        .forEach(entry -> types.put(entry.getKey(), entry.getValue()));
    return true;
  }

  private Map<ElementType, String> methodTypes(IMethodBinding binding) {
    if (binding == null) {
      return Map.of();
    }

    var out = new HashMap<ElementType, String>();
    var declaringClass = binding.getDeclaringClass();

    var caller = methodStack.peek();

    var referencedMethod =
        new ElementType(caller, declaringClass.getQualifiedName(), binding.getName());
    out.put(referencedMethod, accessModifier(binding.getModifiers()));

    var methodDeclaration = binding.getMethodDeclaration();

    if (methodDeclaration.getReturnType().isPrimitive()) {
      return out;
    }

    var voidMethod = methodDeclaration.getReturnType().getName() == "void";
    if (voidMethod) {
      return out;
    }

    var returnType =
        new ElementType(new Type(""), methodDeclaration.getReturnType().getQualifiedName(), "");
    var returnTypeAccess = accessModifier(methodDeclaration.getReturnType().getModifiers());

    out.put(returnType, returnTypeAccess);
    return out;
  }

  public boolean visit(ImportDeclaration node) {
    imports.add(node.getName().getFullyQualifiedName());
    if (node.isOnDemand()) {
      return true;
    }

    types.putIfAbsent(
        new ElementType(new Type(""), node.getName().getFullyQualifiedName(), ""), null);
    return true;
  }

  public Set<Edge> edges() {
    var links =
        types.entrySet().stream()
            .map(
                entry -> {
                  var elementType = entry.getKey();
                  var src = srcNode(elementType.caller);
                  var typeAccess = Optional.ofNullable(entry.getValue()).orElse("public");
                  final var t = elementType.name;

                  var qualifiedImport =
                      imports.stream().filter(importName -> importName.endsWith("." + t)).findAny();

                  if (qualifiedImport.isPresent()) {
                    return new Edge(
                        src,
                        new Node(qualifiedImport.get(), elementType.element, typeAccess, Set.of()));
                  }

                  var pkgImports = new HashSet<>(imports);
                  pkgImports.add(pkg);
                  pkgImports.add(src.label());

                  if (elementType.name.contains(".")
                      && Character.isUpperCase(elementType.name.charAt(0))) {
                    // Type name is prefixed with a class
                    // but not a package. i.e. WrapperClass.NestedEnum
                    var parts = elementType.name.split("\\.");
                    elementType.name = parts[parts.length - 1];

                    var pfx = String.join(".", Arrays.copyOfRange(parts, 0, parts.length - 1));

                    var alreadyImported = pkgImports.stream().anyMatch(imp -> imp.endsWith(pfx));
                    if (!alreadyImported) {
                      pkgImports.add(pkg + "." + pfx);
                    }
                  }

                  return new Edge(
                      src, new Node(elementType.name, elementType.element, typeAccess, pkgImports));
                })
            .collect(Collectors.toSet());

    var out = new HashSet<>(links);
    out.add(new Edge(srcNode(null), null));
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
