package co.packagemap.javaparser.client;

import co.packagemap.javaparser.graph.Edge;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;

public class PackageMapClient {
  private final HttpClient client;
  private final String address;
  private final ObjectMapper mapper;

  public PackageMapClient(String address) {
    this.address = address;
    this.client = HttpClient.newHttpClient();
    this.mapper = new ObjectMapper();
  }

  public String hostedPage(String apiKey, List<Edge> edges)
      throws IOException, InterruptedException {
    List<RequestEdge> requestEdges = new ArrayList<>();
    Set<RequestNode> requestNodes = new HashSet<>();

    edges.forEach(
        edge -> {
          var src = edge.src();

          requestNodes.add(
              new RequestNode(
                  src.name() + "_" + src.element(),
                  src.packageNode().label(),
                  src.unqualifiedName(),
                  src.element(),
                  src.accessModifier()));

          if (edge.linked()) {
            var dst = edge.dst();
            requestEdges.add(
                new RequestEdge(
                    src.name() + "_" + src.element(), dst.name() + "_" + dst.element()));
            requestNodes.add(
                new RequestNode(
                    dst.name() + "_" + dst.element(),
                    dst.packageNode().label(),
                    dst.unqualifiedName(),
                    dst.element(),
                    dst.accessModifier()));
          }
        });

    var request = new RequestGraph(requestNodes, requestEdges);

    var body = mapper.writeValueAsBytes(request);

    var httpRequest =
        HttpRequest.newBuilder(URI.create(address + "/api/map"))
            .header("Authorization", "Basic " + Base64.encodeBase64String(apiKey.getBytes()))
            .POST(BodyPublishers.ofByteArray(body))
            .build();

    var httpResponse = client.send(httpRequest, BodyHandlers.ofString());

    var response = mapper.readValue(httpResponse.body(), Response.class);

    if (response.error() != null && !response.error().isEmpty()) {
      throw new ClientException(response.error());
    }

    return response.url();
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  private record RequestNode(
      String id, String pkg, String name, String element, String accessModifier) {}

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  private record RequestEdge(String srcNodeId, String dstNodeId) {}

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  private record RequestGraph(Set<RequestNode> nodes, List<RequestEdge> edges) {}

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  private record Response(@JsonProperty("url") String url, String id, String error) {}
}
