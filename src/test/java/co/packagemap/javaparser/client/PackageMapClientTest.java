package co.packagemap.javaparser.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import co.packagemap.javaparser.graph.Edge;
import co.packagemap.javaparser.graph.Node;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

public class PackageMapClientTest {

  private static WireMockServer wireMockServer;
  private static PackageMapClient client;

  private static final boolean IGNORE_ARRAY_ORDER = true;
  private static final boolean IGNORE_EXTRA_ELEMENTS = false;

  @BeforeClass
  public static void setupWiremock() {
    wireMockServer = new WireMockServer(0);
    wireMockServer.start();
    client = new PackageMapClient(wireMockServer.baseUrl());
  }

  @Test
  public void shouldMakeCorrectRequest() throws IOException, InterruptedException {
    configureFor(wireMockServer.port());
    stubFor(
        post(WireMock.urlPathEqualTo("/api/map"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
            {"url": "localhost:8080/map/uuid-abc"}
            """)));

    var rootNode = new Node("foo.bar.Root", "", "public", Set.of());
    var nest1 = new Node("foo.bar.nested.Nest1", "", "public", Set.of());
    var nest2 = new Node("foo.bar.nested.Nest2", "", "public", Set.of());

    var edges = List.of(new Edge(rootNode, nest1), new Edge(nest1, nest2));

    var url = client.hostedPage("foo:bar", edges);

    Assertions.assertThat(url).isEqualTo("localhost:8080/map/uuid-abc");

    verify(
        postRequestedFor(urlEqualTo("/api/map"))
            .withBasicAuth(new BasicCredentials("foo", "bar"))
            .withRequestBody(
                equalToJson(
                    """
            {
              "nodes": [
                {"id": "foo_bar_Root_", "pkg": "foo.bar", "name": "Root", "element": "", "access_modifier": "public"},
                {"id": "foo_bar_nested_Nest1_", "pkg": "foo.bar.nested", "name": "Nest1", "element": "", "access_modifier": "public"},
                {"id": "foo_bar_nested_Nest2_", "pkg": "foo.bar.nested", "name": "Nest2", "element": "", "access_modifier": "public"}
              ],
              "edges": [
                {"src_node_id": "foo_bar_Root_", "dst_node_id": "foo_bar_nested_Nest1_"},
                {"src_node_id": "foo_bar_nested_Nest1_", "dst_node_id": "foo_bar_nested_Nest2_"}
              ]
            }
            """,
                    IGNORE_ARRAY_ORDER,
                    IGNORE_EXTRA_ELEMENTS)));
  }
}
