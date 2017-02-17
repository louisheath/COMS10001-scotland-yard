public class ExceptionalCalculator {

  public static void main (String[] args) {
    Adder adder = new Adder();
    try {
      for (String arg : args) {
	 adder.add(Integer.parseInt(arg));
      }
      System.out.println("Sum:" + adder.sum);
    } catch (Exception e) {
      System.out.println("Something went wrong, but I can handle it!");
} } }
