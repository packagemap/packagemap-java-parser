package co.packagemap.javaparser.sourcecode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jdt.core.dom.CompilationUnit;

record SourceFile(Path path) implements SourceAST {

  private char[] read() throws IOException {
    return Files.readString(path).toCharArray();
  }

  @Override
  public String originFile() {
    return path.toString();
  }

  @Override
  public CompilationUnit toAST(ParserFactory parserFactory) throws IOException {
    var parser = parserFactory.newParser(path.getFileName().toString());
    parser.setSource(read());

    return (CompilationUnit) parser.createAST(null);
  }
}
