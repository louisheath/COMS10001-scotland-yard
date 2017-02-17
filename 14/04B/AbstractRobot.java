abstract class AbstractRobot extends Robot {
  abstract void greet(AbstractRobot other);
  abstract void greet(TranslationRobot other);
  abstract void greet(CarrierRobot other);   
}