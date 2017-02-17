class Robot {
	
  String name;
  int numLegs;
  float powerLevel;

  Robot(String name) {
    this.name = name;
    numLegs = 2;
    powerLevel = 2.0f;
  }
  
  Robot() {
    this("Standard Model");
  }

  void talk(String phrase) {
    if (powerLevel >= 1.0f) {
      System.out.println(name + " says " + phrase);
      powerLevel -= 1.0f;
    } else {
      System.out.println(name + " is too weak to talk.");
    }
  }

  void charge(float amount) {
    System.out.println(name + " charges.");
    powerLevel = powerLevel + amount;
  }
}