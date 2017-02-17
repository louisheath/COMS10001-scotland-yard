class PositionWorld {

  public static void main (String[] args) {
    Position[] positions = new Position[3];
    int i = 1;
    for (int index = 0; index<positions.length; index++) {
      positions[index] = new Position(1,i++);
    }
    positions[2].move(1,1);
    positions[2].move(-1,-2);
    positions[2] = positions[1];
    System.out.println(positions[1].x + " " +positions[1].y);
    positions[1].move(0,5);
    System.out.println(positions[1].x + " " +positions[1].y);
  }
}
