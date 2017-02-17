class DispatchWorld {

  public static void main (String[] args) {
	AbstractRobot c3po = new TranslationRobot("e");
	AbstractRobot c4po = new TranslationRobot("o");
	AbstractRobot c5po = new CarrierRobot();
	AbstractRobot c6po = new CarrierRobot();
	c3po.greet(c4po);
	c5po.greet(c4po);
	c4po.greet(c5po);
	c5po.greet(c6po);
} }
