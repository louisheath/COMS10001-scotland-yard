public class RobotArrays {
	
  static Robot[] makeRobotArray() {
    Robot[] robots = {             
      new Robot("C3PO"), 
      new Robot("C4PO"), 
      new Robot("C5PO")
    };
    return robots;
  }
  
  static void printArray(Robot[] robots) {
    for (Robot robot : robots)      
      System.out.println(robot.name);  
  }
  
  public static void main (String[] args) {
    Robot[] robots = makeRobotArray();
    printArray(robots);
  }
}