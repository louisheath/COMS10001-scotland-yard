class CallWorld {

  static int square(Number n) {
    n.x *= n.x;
    return n.x;
  }

  static int square(int x) {
    x *= x;
    return x;
  }

  public static void main (String[] args) {
    int x = 10;
    System.out.println(square(x));
    System.out.println(x);
    Number n = new Number(10);
    System.out.println(square(n));
    System.out.println(n.x);	
  }
}
