class Calculator {
	
  public static void main (String[] args) {
    Adder adder = new Adder();
    for (String arg : args) {
      adder.add(Integer.parseInt(arg));
    }
    System.out.println("Sum:" + adder.sum);
  } 
}
