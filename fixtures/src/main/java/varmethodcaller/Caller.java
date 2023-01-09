package varmethodcaller;

public class Caller {

  public static void myMethod() {
    var used = User.myStaticMethod();
    var user = new User();
    user.myMethod();
  }
}
