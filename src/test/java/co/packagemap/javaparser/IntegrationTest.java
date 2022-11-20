package co.packagemap.javaparser;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import co.packagemap.javaparser.client.PackageMapClient;
import co.packagemap.javaparser.graph.Parser;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntegrationTest {

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

  @Before
  public void setup() {
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
  }

  @Test
  public void shouldHandleNestedEnum() throws Exception {
    var edges = Parser.findEdges(List.of("./fixtures/src/main/java/nestedenum/"), null, null, null);

    client.hostedPage("bar:foo", edges);
    verify(
        postRequestedFor(urlEqualTo("/api/map"))
            .withBasicAuth(new BasicCredentials("bar", "foo"))
            .withRequestBody(
                equalToJson(
                    """
     {
      "nodes" : [ {
        "id" : "nestedenum_WrapperClass_NestedEnum",
        "pkg" : "nestedenum.WrapperClass",
        "name" : "NestedEnum",
        "access_modifier" : "public"
      }, {
        "id" : "nestedenum_WrapperClass",
        "pkg" : "nestedenum",
        "name" : "WrapperClass",
        "access_modifier" : "public"
      }, {
        "id" : "nestedenum_OtherClass",
        "pkg" : "nestedenum",
        "name" : "OtherClass",
        "access_modifier" : "public"
      } ],
      "edges" : [ {
        "src_node_id" : "nestedenum_WrapperClass",
        "dst_node_id" : "nestedenum_WrapperClass_NestedEnum"
      }, {
        "src_node_id" : "nestedenum_OtherClass",
        "dst_node_id" : "nestedenum_WrapperClass_NestedEnum"
      } ]
    }
            """,
                    IGNORE_ARRAY_ORDER,
                    IGNORE_EXTRA_ELEMENTS)));
  }
}
