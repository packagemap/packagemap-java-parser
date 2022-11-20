package co.packagemap.javaparser;

import co.packagemap.javaparser.client.ClientException;
import co.packagemap.javaparser.client.PackageMapClient;
import co.packagemap.javaparser.graph.Parser;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {

  private static final String ADDRESS = "https://packagemap.co";

  public static void main(String[] args) throws IOException {
    var options = new Options();

    var base =
        new Option("b", "base", true, "the base packages, all edges must be inside this base");
    var filterFlag = new Option("f", "filter", true, "only include packages with this prefix");
    var excludeFlag = new Option("e", "exclue", true, "don't include packages with this prefix");
    var gitFlag =
        new Option("g", "git", true, "show only changed files from branch or commit hash");
    var remote =
        new Option("r", "remote-url", true, "URL of the remote server to use, default: " + ADDRESS);
    var debug = new Option("d", "debug", false, "print debug");
    var key = new Option("k", "key", true, "api key in 'user_id:secret_key' format");

    options.addOption(base);
    options.addOption(filterFlag);
    options.addOption(excludeFlag);
    options.addOption(gitFlag);
    options.addOption(remote);
    options.addOption(debug);
    options.addOption(key);

    CommandLineParser cmdParser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;

    try {
      cmd = cmdParser.parse(options, args);
    } catch (ParseException e) {
      formatter.printHelp("parser [FLAGS] [DIRS must be root of packages]", options);
      System.exit(1);
    }

    if (!cmd.hasOption(key)) {
      formatter.printHelp(
          "parser [FLAGS] [DIRS must be root of packages]", "missing api key", options, null);
      System.exit(1);
    }

    List<String> filterBy = extractArgs(cmd, filterFlag);
    List<String> excludeBy = extractArgs(cmd, excludeFlag);

    if (cmd.hasOption(gitFlag)) {
      filterBy = GitHistoryParser.packagesForHistory(cmd.getOptionValue(gitFlag), cmd.getArgList());
      System.out.println(filterBy);
    }

    var edgeList =
        Parser.findEdges(cmd.getArgList(), cmd.getOptionValue(base, ""), filterBy, excludeBy);

    var address = ADDRESS;
    if (cmd.hasOption(remote)) {
      address = cmd.getOptionValue(remote);
    }

    if (cmd.hasOption(debug)) {
      edgeList.stream()
          .forEach(
              e -> {
                System.out.println(e.prettyPrint());
              });
      return;
    }

    try {
      var client = new PackageMapClient(address);
      System.out.println(client.hostedPage(cmd.getOptionValue(key), edgeList));
    } catch (ClientException e) {
      System.out.println("error: " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private static List<String> extractArgs(CommandLine cmd, Option flag) {
    if (!cmd.hasOption(flag)) {
      return null;
    }

    return Arrays.asList(cmd.getOptionValues(flag));
  }
}
