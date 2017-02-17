public class TranslationRobot extends AbstractRobot {
  // class has everything that AbstractRobot has implicitly
  String substitute;
  
  TranslationRobot(String substitute) {
    this.substitute = substitute;
  }
  
  void translate(String phrase) {
    this.talk(phrase.replaceAll("a", substitute));  
  }

  @Override
  void greet(TranslationRobot other) {
    talk("'Hello from a TranslationRobot to another.'"); 
  }

  @Override
  void greet(CarrierRobot other) {
    talk("'Hello from a CarrierRobot to a TranslationRobot.'"); 
  }

  @Override
  void greet(AbstractRobot other) {
    other.greet(this); 
  }
   
  @Override
  void charge(float amount) {
    System.out.println(name + " charges double.");
    powerLevel = powerLevel + 2 * amount;
} }
