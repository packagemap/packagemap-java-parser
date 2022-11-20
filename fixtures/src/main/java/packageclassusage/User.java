package packageclassusage;

public class User {

  public static Used myStaticMethod() {
    new Used();
  }

  public Used myMethod() {
    new Used();
  }
}
