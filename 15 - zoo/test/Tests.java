import org.junit.*;
import static org.junit.Assert.*;

public class Tests {

  @Test
  public void test1() {
    Zoo zoo = new Zoo();
    Animal animal = new Animal();
    Food food = new Food();

    String output = zoo.feed(animal, food);

    assertEquals("Feeding Food to an Animal:", "animal eats food", output);
  }

  @Test
  public void test2() {
    Zoo zoo = new Zoo();
    Dog scooby = new Dog();
    Food food = new Food();

    String output = zoo.feed(scooby, food);

    assertEquals("Feeding Food to a Dog:", "dog eats food", output);
  }

  @Test
  public void test3() {
    Zoo zoo = new Zoo();
    Animal[] animals = new Animal[]
    { new Animal()
    , new Dog ()
    , new Cat ()
    };

    Food[] foods = new Food[]
    { new Food()
    , new Fruit()
    , new Chocolate()
    };

    String[] outputs = new String[9];
    for (int i = 0; i < animals.length; i++) {
      for (int j = 0; j < foods.length; j++) {
        outputs[(i * 3) + j] = zoo.feed(animals[i], foods[j]);
      }
    }

    assertEquals("Feeding Foods to Animals:", "animal eats food", outputs[0]);
    assertEquals("Feeding Foods to Animals:", "animal eats fruit", outputs[1]);
    assertEquals("Feeding Foods to Animals:", "animal eats chocolate", outputs[2]);
    assertEquals("Feeding Foods to Animals:", "dog eats food", outputs[3]);
    assertEquals("Feeding Foods to Animals:", "dog eats fruit", outputs[4]);
    assertEquals("Feeding Foods to Animals:", "dog eats chocolate", outputs[5]);
    assertEquals("Feeding Foods to Animals:", "cat eats food", outputs[6]);
    assertEquals("Feeding Foods to Animals:", "cat eats fruit", outputs[7]);
    assertEquals("Feeding Foods to Animals:", "cat eats chocolate", outputs[8]);
  }

}
