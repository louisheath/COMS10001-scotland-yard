import org.junit.runner.*;
import org.junit.runner.notification.*;


public class TestRunner {

  public static void main(String[] args) {
    try {
      Result result = JUnitCore.runClasses(Tests.class);
      boolean passed = true;
      System.out.println("-----------TESTS----------");
      for (Failure failure : result.getFailures()) {
        System.out.println(failure.getTestHeader() + ":");
        System.out.println(failure.getMessage());
        passed = false;
      }
      if (passed) System.out.println("You pass all the tests!");
      System.out.println("--------------------------");
    } catch (AssertionError e) {}
  }

}
