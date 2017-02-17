import java.io.*;

class RobotHeap {

  public static void main (String[] args) {
    Robot c3po = new Robot("C3PO");
    Robot c4po = new Robot("C4PO");
    Robot handle = c3po;
    c4po = handle;
    c3po = null;
    PrintStream out = System.out;
    String fooA = "C3PO";
    String fooB = "C3PO";
    String fooC = new String("C3PO");
    out.println(fooA==fooB);             //true
    out.println(fooA==fooC);             //false
    out.println(fooC.equals(fooA));      //true
    out.println(c3po==handle);           //false
    out.println(c4po==handle);           //true
    out.println(c3po==null);             //true
    out.println(c4po==handle);           //true
    out.println(c4po.name==fooC);        //false
    out.println(c4po.name==fooB);        //true
    out.println(fooC.equals(c4po.name)); //true
} }
