package co.packagemap.javaparser.sourcecode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class ParserFactory {

  private List<String> sources;

  public ParserFactory(List<String> sources) {
    this.sources = sources;
  }

  public ASTParser newParser(String fileName) {
    ASTParser parser = ASTParser.newParser(AST.JLS18);

    Map<String, String> options = JavaCore.getOptions();
    JavaCore.setComplianceOptions(JavaCore.VERSION_18, options);
    parser.setCompilerOptions(options);
    parser.setResolveBindings(true);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    parser.setBindingsRecovery(true);
    parser.setUnitName(fileName);

    String[] classpath = new String[] {System.getProperty("java.class.path")};
    var encodings =
        sources.stream().map(s -> "UTF-8").collect(Collectors.toList()).toArray(new String[0]);
    parser.setEnvironment(classpath, sources.toArray(new String[0]), encodings, true);

    return parser;
  }
}
