package varmethodcaller;

public class User {

  protected static Used myStaticMethod() {
    return new Used();
  }

  String myMethod() {
    internalMethodCall();
    return "foo";
  }

  private void internalMethodCall() {}

  public String myReference() {
    return "foo";
  }
}
