package varmethodcaller;

public class Caller {

  public static void callingMethod() {
    var used = User.myStaticMethod();
    var user = new User();
    user.myMethod();
  }
}
