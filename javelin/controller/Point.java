/**
 *
 */
package javelin.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
		final Point p=(Point)obj;
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
	 *         2-dimensional array (not that max values are exclusive).
	 */
	public boolean validate(int minx,int miny,int maxx,int maxy){
		return minx<=x&&x<maxx&&miny<=y&&y<maxy;
	}

	public double distance(Point p){
		final int deltax=Math.abs(x-p.x);
		final int deltay=Math.abs(y-p.y);
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
		Point[] adjacent=new Point[8];
		int i=0;
		for(int x=-1;x<=+1;x++)
			for(int y=-1;y<=+1;y++){
				if(x==0&&y==0) continue;
				adjacent[i]=new Point(x,y);
				i+=1;
			}
		return adjacent;
	}

	/**
	 * @return 8 points adjacent to this point (neighbors).
	 */
	public List<Point> getadjacent(){
		var adjacent=new ArrayList<Point>(8);
		for(int x=-1;x<=+1;x++)
			for(int y=-1;y<=+1;y++)
				if(x!=0||y!=0) adjacent.add(new Point(this.x+x,this.y+y));
		return adjacent;
	}

	/**
	 * @return 4 points orthogonally adjacent to this point (straight up, down,
	 *         left and right).
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
	 * @param yp Initial y, inclusive.
	 * @param tox Final x, exclusive.
	 * @param toy Final y, exclusive.
	 * @return A list containig all the Points in the given range.
	 */
	public static HashSet<Point> getrange(int xp,int yp,int tox,int toy){
		HashSet<Point> range=new HashSet<>((tox-xp)*(toy-yp));
		for(var x=xp;x<tox;x++)
			for(int y=yp;y<toy;y++)
				range.add(new Point(x,y));
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
		if(RPG.chancein(2))
			x+=RPG.chancein(2)?+1:-1;
		else
			y+=RPG.chancein(2)?+1:-1;
	}
}