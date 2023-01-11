package co.packagemap.javaparser.graph;

import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class NodeTest {

  @Test
  public void packageNodeEnum() {
    var n = new Node("com.site.WrapperClass.EnumName", "", "public", Set.of());

    Assertions.assertThat(n.packageNode().label()).isEqualTo("com.site.WrapperClass");
  }

  @Test
  public void extractsPackage() {
    var n = new Node("com.site.WrapperClass", "", "public", Set.of());

    Assertions.assertThat(n.packageNode().label()).isEqualTo("com.site");
  }

  @Test
  public void extractsPackageEmpty() {
    var n = new Node("WrapperClass", "", "public", Set.of());

    Assertions.assertThat(n.packageNode().label()).isEqualTo("");
  }

  @Test
  public void extractsPackageDot() {
    var n = new Node(".WrapperClass", "", "public", Set.of());

    Assertions.assertThat(n.packageNode().label()).isEqualTo("");
  }

  @Test
  public void expectClassPrefixHasNoPackage() {
    var n = new Node("WrapperClass.EnumName", "", "public", Set.of());

    Assertions.assertThat(n.packageNode().label()).isEqualTo("");
  }
}
