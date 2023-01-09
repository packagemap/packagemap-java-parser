package varmethodcaller;

public class User {

  public static Used myStaticMethod() {
    return new Used();
  }

  public String myMethod() {
    return "foo";
  }

  public String myReference() {
    return "foo";
  }
}
