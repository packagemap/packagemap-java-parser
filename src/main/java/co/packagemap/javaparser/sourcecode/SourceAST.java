package co.packagemap.javaparser.sourcecode;

import java.io.IOException;
import org.eclipse.jdt.core.dom.CompilationUnit;

public interface SourceAST {

  public String originFile();

  public CompilationUnit toAST(ParserFactory parserFactory) throws IOException;
}
