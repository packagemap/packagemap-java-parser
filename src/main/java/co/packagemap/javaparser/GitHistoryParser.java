package co.packagemap.javaparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class GitHistoryParser {

  static List<String> packagesForHistory(String mainBrachName, List<String> files) {
    return files.stream()
        .flatMap(
            file -> {
              try {
                return runProcess(mainBrachName, file);
              } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return Stream.empty();
              }
            })
        .distinct()
        .toList();
  }

  private static Stream<String> runProcess(String mainBrachName, String file)
      throws IOException, InterruptedException {
    ProcessBuilder pb =
        new ProcessBuilder()
            .command("git", "diff", "--name-only", mainBrachName)
            .directory(new File(file));

    Process p = pb.start();

    StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
    outputGobbler.start();
    p.waitFor();
    outputGobbler.join();

    var prefix = "/src/main/java/";

    return outputGobbler.lines().stream()
        .filter(s -> s.contains(prefix))
        .map(
            s -> {
              var start = s.indexOf(prefix) + prefix.length();
              var end = s.lastIndexOf(".java");
              return s.substring(start, end).replaceAll("/", ".");
            });
  }

  private static class StreamGobbler extends Thread {

    InputStream is;

    List<String> lines = new ArrayList<>();

    private StreamGobbler(InputStream is) {
      this.is = is;
    }

    public List<String> lines() {
      return lines;
    }

    @Override
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          lines.add(line);
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
