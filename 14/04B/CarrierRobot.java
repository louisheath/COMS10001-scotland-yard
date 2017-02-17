class CarrierRobot extends AbstractRobot {

  @Override
  void greet(TranslationRobot other) {
    talk("'Hello from a TranslationRobot to a CarrierRobot.'"); 
  }

  @Override
  void greet(CarrierRobot other) {
    talk("'Hello from a CarrierRobot to another.'"); 
  }

  @Override 
  void greet(AbstractRobot other) {
    other.greet(this); 
  }

  void carry() {
    System.out.println("Carrying.");
} }
