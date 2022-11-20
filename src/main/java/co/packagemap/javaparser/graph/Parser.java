package co.packagemap.javaparser.graph;

import co.packagemap.javaparser.sourcecode.ParserFactory;
import co.packagemap.javaparser.sourcecode.SourceAST;
import co.packagemap.javaparser.sourcecode.SourceFiles;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Parser {

  public static List<Edge> findEdges(
      List<String> dirs, String base, List<String> filterPackages, List<String> excludePackages)
      throws IOException {

    List<SourceAST> files =
        dirs.stream().flatMap(arg -> SourceFiles.sourceFiles(arg).stream()).toList();

    var factory = new ParserFactory(dirs);

    Map<Edge, Boolean> allEdges = new ConcurrentHashMap<>();
    files.parallelStream()
        .flatMap(
            sourceFile -> {
              CompilationUnit ast;
              try {
                ast = sourceFile.toAST(factory);
              } catch (IOException e) {
                e.printStackTrace();
                return Stream.empty();
              }

              var visitor = new ASTTypeVisitor();

              ast.accept(visitor);
              return visitor.edges().stream();
            })
        .forEach(edge -> allEdges.put(edge, true));

    var edgeList = new ArrayList<>(allEdges.keySet());
    Set<Node> allNodes =
        edgeList.stream().flatMap(edge -> edge.nodes().stream()).collect(Collectors.toSet());

    for (int i = 0; i < edgeList.size(); i++) {
      var edge = edgeList.get(i);

      if (!edge.linked()) {
        continue;
      }

      var dstQualified = !edge.dst().unqualified();

      if (dstQualified) {
        continue;
      }

      final var index = i;

      allNodes.stream()
          .filter(n -> edge.dst().qualifiedBy(n))
          .findFirst()
          .ifPresent(replacement -> edgeList.set(index, new Edge(edge.src(), replacement)));
    }

    var filteredEdges =
        edgeList.stream()
            .filter(e -> e.base(base))
            .filter(filterPredicate(filterPackages))
            .filter(excludePredicate(excludePackages))
            .map(
                edge -> {
                  if (!edge.linked()) {
                    return new Edge(
                        new Node(edge.src().label(), edge.src().accessModifier(), Set.of()), null);
                  }

                  return new Edge(
                      new Node(edge.src().label(), edge.src().accessModifier(), Set.of()),
                      new Node(edge.dst().label(), edge.dst().accessModifier(), Set.of()));
                })
            .distinct()
            .toList();

    return filteredEdges;
  }

  private static Predicate<Edge> filterPredicate(List<String> filterBy) {
    if (filterBy == null) {
      return edge -> true;
    }

    return edge -> {
      return filterBy.stream().anyMatch(pkg -> edge.hasPrefix(pkg));
    };
  }

  private static Predicate<Edge> excludePredicate(List<String> filterBy) {
    if (filterBy == null) {
      return edge -> true;
    }

    return edge -> {
      return !filterBy.stream().anyMatch(pkg -> edge.hasPrefix(pkg));
    };
  }
}
