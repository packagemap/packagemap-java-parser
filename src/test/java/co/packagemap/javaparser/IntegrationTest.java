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
        "id" : "nestedenum_WrapperClass_NestedEnum_",
        "pkg" : "nestedenum.WrapperClass",
        "name" : "NestedEnum",
        "element": "",
        "access_modifier" : "public"
      }, {
        "id" : "nestedenum_WrapperClass_",
        "pkg" : "nestedenum",
        "name" : "WrapperClass",
        "element": "",
        "access_modifier" : "public"
      }, {
        "id" : "nestedenum_OtherClass_",
        "pkg" : "nestedenum",
        "name" : "OtherClass",
        "element": "",
        "access_modifier" : "public"
      } ],
      "edges" : [ {
        "src_node_id" : "nestedenum_WrapperClass_",
        "dst_node_id" : "nestedenum_WrapperClass_NestedEnum_"
      }, {
        "src_node_id" : "nestedenum_OtherClass_",
        "dst_node_id" : "nestedenum_WrapperClass_NestedEnum_"
      } ]
    }
            """,
                    IGNORE_ARRAY_ORDER,
                    IGNORE_EXTRA_ELEMENTS)));
  }

  @Test
  public void shouldIncludeMethodsAndAccess() throws Exception {
    var edges =
        Parser.findEdges(List.of("./fixtures/src/main/java/"), "varmethodcaller", null, null);

    client.hostedPage("bar:foo", edges);
    verify(
        postRequestedFor(urlEqualTo("/api/map"))
            .withBasicAuth(new BasicCredentials("bar", "foo"))
            .withRequestBody(
                equalToJson(
                    """
    {
      "nodes" : [ {
        "id" : "varmethodcaller_User_myStaticMethod",
        "pkg" : "varmethodcaller",
        "name" : "User",
        "element" : "myStaticMethod",
        "access_modifier" : "protected"
      }, {
        "id" : "varmethodcaller_User_internalMethodCall",
        "pkg" : "varmethodcaller",
        "name" : "User",
        "element" : "internalMethodCall",
        "access_modifier" : "private"
      }, {
        "id" : "varmethodcaller_User_myMethod",
        "pkg" : "varmethodcaller",
        "name" : "User",
        "element" : "myMethod",
        "access_modifier" : "package_private"
      }, {
        "id" : "varmethodcaller_Caller_callingMethod",
        "pkg" : "varmethodcaller",
        "name" : "Caller",
        "element" : "callingMethod",
        "access_modifier" : "public"
      }, {
        "id" : "varmethodcaller_Used_",
        "pkg" : "varmethodcaller",
        "name" : "Used",
        "element" : "",
        "access_modifier" : "public"
      }, {
        "id" : "varmethodcaller_User_",
        "pkg" : "varmethodcaller",
        "name" : "User",
        "element" : "",
        "access_modifier" : "public"
      }, {
        "id" : "varmethodcaller_Caller_",
        "pkg" : "varmethodcaller",
        "name" : "Caller",
        "element" : "",
        "access_modifier" : "public"
      } ],
      "edges" : [ {
        "src_node_id" : "varmethodcaller_User_myMethod",
        "dst_node_id" : "varmethodcaller_User_internalMethodCall"
      }, {
        "src_node_id" : "varmethodcaller_User_",
        "dst_node_id" : "varmethodcaller_Used_"
      }, {
        "src_node_id" : "varmethodcaller_Caller_callingMethod",
        "dst_node_id" : "varmethodcaller_User_myMethod"
      }, {
        "src_node_id" : "varmethodcaller_Caller_callingMethod",
        "dst_node_id" : "varmethodcaller_User_myStaticMethod"
      }, {
        "src_node_id" : "varmethodcaller_Caller_",
        "dst_node_id" : "varmethodcaller_Used_"
      }, {
        "src_node_id" : "varmethodcaller_Caller_",
        "dst_node_id" : "varmethodcaller_User_"
      } ]
    }
            """,
                    IGNORE_ARRAY_ORDER,
                    IGNORE_EXTRA_ELEMENTS)));
  }
}
