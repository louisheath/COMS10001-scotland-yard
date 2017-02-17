class Position {
  int x;
  int y;
  
  Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  void move(int dx, int dy) {
    x += dx; 
    y += dy;
  }
}