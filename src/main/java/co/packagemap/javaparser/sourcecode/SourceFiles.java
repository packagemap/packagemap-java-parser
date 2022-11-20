package co.packagemap.javaparser.sourcecode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

public class SourceFiles {
  public static List<SourceAST> sourceFiles(String dir) {
    Collection<File> files =
        FileUtils.listFiles(
            new File(dir), new RegexFileFilter("^(.*?)\\.java$"), DirectoryFileFilter.DIRECTORY);

    var out = new ArrayList<SourceAST>();

    for (var f : files) {
      out.add(new SourceFile(f.getAbsoluteFile().toPath()));
    }

    return out;
  }
}
