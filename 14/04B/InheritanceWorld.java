public class InheritanceWorld {

  public static void main (String[] args) {
    Robot c3po = new Robot();
    TranslationRobot c4po = new TranslationRobot("e");
    Robot c5po = new TranslationRobot("e");
    c3po.charge(10); //charges normally
    c4po.charge(10); //charges double
    c5po.charge(10); //polymorphism: charges double!
} }
