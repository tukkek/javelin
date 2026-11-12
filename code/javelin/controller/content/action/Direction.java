package javelin.controller.content.action;

import javelin.controller.Point;

/// A directional arrow and a delta-point
public class Direction{
  /// South-west
  static public Direction SW=new Direction('↙',new Point(-1,+1));
  /// West
  static public Direction W=new Direction('←',new Point(-1,0));
  /// North-west
  static public Direction NW=new Direction('↖',new Point(-1,-1));
  /// North
  static public Direction N=new Direction('↑',new Point(0,-1));
  /// North-east
  static public Direction NE=new Direction('↗',new Point(+1,-1));
  /// East
  static public Direction E=new Direction('→',new Point(+1,0));
  /// South-east
  static public Direction SE=new Direction('↘',new Point(+1,+1));
  /// South
  static public Direction S=new Direction('↓',new Point(0,+1));

  Character arrow;
  Point point;

  /// Constructor
  public Direction(Character c,Point p){
    arrow=c;
    point=p;
  }
}