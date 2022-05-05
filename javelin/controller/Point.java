/**
 *
 */
package javelin.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.old.RPG;

/**
 * X Y coordinate.
 *
 * @author alex
 */
public class Point implements Cloneable,Serializable{
  public int x;
  public int y;

  public Point(final int x,final int y){
    this.x=x;
    this.y=y;
  }

  public Point(Point p){
    this(p.x,p.y);
  }

  public Point(Combatant c){
    x=c.location[0];
    y=c.location[1];
  }

  @Override
  public boolean equals(final Object obj){
    final var p=(Point)obj;
    return p.x==x&&p.y==y;
  }

  @Override
  public String toString(){
    return x+":"+y;
  }

  @Override
  public Point clone(){
    try{
      return (Point)super.clone();
    }catch(final CloneNotSupportedException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public int hashCode(){
    return Objects.hash(x,y);
  }

  /**
   * @return <code>true</code> if this is valid inside the bounds of a
   *   2-dimensional array (note that max values are exclusive).
   */
  public boolean validate(int minx,int miny,int maxx,int maxy){
    return minx<=x&&x<maxx&&miny<=y&&y<maxy;
  }

  public double distance(Point p){
    final var deltax=Math.abs(x-p.x);
    final var deltay=Math.abs(y-p.y);
    return Math.sqrt(deltax*deltax+deltay*deltay);
  }

  public int distanceinsteps(Point p){
    return Walker.distanceinsteps(x,y,p.x,p.y);
  }

  /**
   * @deprecated Use {@link #getadjacent()}
   */
  @Deprecated
  static public Point[] getadjacent2(){
    var adjacent=new Point[8];
    var i=0;
    for(var x=-1;x<=+1;x++) for(var y=-1;y<=+1;y++){
      if(x==0&&y==0) continue;
      adjacent[i]=new Point(x,y);
      i+=1;
    }
    return adjacent;
  }

  /** @return As {@link #getadjacent()} but with arbitrary range. */
  public List<Point> getadjacent(int range){
    var side=range*2+1;
    var adjacent=new ArrayList<Point>(side*side-1);
    for(var x=-range;x<=+range;x++) for(var y=-range;y<=+range;y++)
      if(x!=0||y!=0) adjacent.add(new Point(this.x+x,this.y+y));
    return adjacent;
  }

  /** @return 8 points adjacent to this point (neighbors). */
  public List<Point> getadjacent(){
    return getadjacent(1);
  }

  /**
   * @return 4 points orthogonally adjacent to this point (straight up, down,
   *   left and right).
   */
  public List<Point> getorthogonallyadjacent(){
    return List.of(new Point(x-1,y),new Point(x+1,y),new Point(x,y-1),
        new Point(x,y+1));
  }

  /**
   * @deprecated Use {@link #getorthogonallyadjacent()}.
   */
  @Deprecated
  static public Point[] getadjacentorthogonal(){
    return new Point[]{new Point(-1,0),new Point(+1,0),new Point(0,-1),
        new Point(0,+1)};
  }

  /**
   * @param x Initial x, inclusive.
   * @param fromy Initial y, inclusive.
   * @param tox Final x, exclusive.
   * @param toy Final y, exclusive.
   * @return A list containig all the Points in the given range.
   */
  public static HashSet<Point> getrange(int fromx,int fromy,int tox,int toy){
    var range=new HashSet<Point>((tox-fromx)*(toy-fromy));
    for(var x=fromx;x<tox;x++)
      for(var y=fromy;y<toy;y++) range.add(new Point(x,y));
    return range;
  }

  /** Displace each axis between [-1,+1]. */
  public void displace(){
    x+=RPG.r(-1,+1);
    y+=RPG.r(-1,+1);
  }

  /**
   * Like {@link #displace()} but only on one axis at a time.
   */
  public void displaceaxis(){
    if(RPG.chancein(2)) x+=RPG.chancein(2)?+1:-1;
    else y+=RPG.chancein(2)?+1:-1;
  }

  /**
   * Raises or lowers {@link #x} and {@link #y} to fit the given ranges.
   *
   * @param maxx Exclusive.
   * @param maxy Exclusive.
   */
  public void bind(int minx,int miny,int maxx,int maxy){
    if(x<minx) x=minx;
    else if(x>=maxx) x=maxx-1;
    if(y<miny) y=miny;
    else if(y>=maxy) y=maxy-1;
  }

  /** @return {@link #getrange(int, int, int, int)} but from origin. */
  public static Set<Point> getrange(int maxx,int maxy){
    return getrange(0,0,maxx,maxy);
  }

  /** @return As {@link #validate(int, int, int, int)} but from origin. */
  public boolean validate(int maxx,int maxy){
    return validate(0,0,maxx,maxy);
  }

  /** As {@link #bind(int, int)} but from origin. */
  public void bind(int maxx,int maxy){
    bind(0,0,maxx,maxy);
  }
}
