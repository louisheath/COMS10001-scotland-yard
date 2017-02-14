public class Cat extends Animal {

  @Override
  public String eat(Food food) {
      return food.eaten(this);
  }
}
