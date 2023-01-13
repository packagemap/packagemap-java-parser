package varmethodcaller;

import java.util.List;

public class Caller {

  public static void callingMethod() {
    var used = User.myStaticMethod();
    var user = new User();
    user.myMethod();

    List.of(user).map(user::myReference).forEach(x -> System.out.println(x));
  }
}
