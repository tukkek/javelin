package javelin.controller.map.terrain.forest;

import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Branching path through a forest.
 *
 * @author alex
 */
public class ForestPath extends Map{
	/** Number of branching paths. */
	protected int paths=RPG.r(1,4)+1;
	/** Percentage of {@link Square#obstructed}. */
	protected float obstructed=RPG.r(0,30)/100f;
	/** If <code>true</code>, adds a river. */
	protected boolean river=RPG.chancein(6);
	/**
	 * Array of {minimum,maximum} width to stretch the river - used for 4
	 * directions, (not 2 axis).
	 */
	protected int[] riverwidth=new int[]{0,1};

	/** Constructor. */
	public ForestPath(){
		super("Forest path",DndMap.SIZE,DndMap.SIZE);
		Medium.standarize(this);
		obstacle=Images.get(List.of("terrain","bush2"));
	}

	private void flood(){
		if(!river) return;
		HashSet<Point> path=null;
		while(path==null||path.size()<map.length)
			path=drawriver();
		var w=riverwidth;
		var deltax=new int[]{RPG.r(w[0],w[1]),RPG.r(w[0],w[1])};
		var deltay=new int[]{RPG.r(w[0],w[1]),RPG.r(w[0],w[1])};
		for(var r:path)
			for(var x=r.x-deltax[0];x<=r.x+deltax[1];x++)
				for(var y=r.y-deltay[0];y<=r.y+deltay[0];y++)
					if(validate(x,y)){
						map[x][y].clear();
						map[x][y].flooded=true;
					}
	}

	HashSet<Point> drawriver(){
		var river=new HashSet<Point>();
		var width=map.length;
		var height=map[0].length;
		var root=new Point(RPG.r(0,width),RPG.r(0,height));
		if(RPG.chancein(2))
			root.x=RPG.chancein(2)?0:width-1;
		else
			root.y=RPG.chancein(2)?0:height-1;
		var p=root;
		while(p.validate(0,0,width,height)){
			river.add(p);
			p=new Point(p);
			if(RPG.chancein(2)){
				p.x+=root.x>0?-1:+1;
				p.y+=RPG.r(-1,+1);
			}else{
				p.x+=RPG.r(-1,+1);
				p.y+=root.y>0?-1:+1;
			}
		}
		return river;
	}

	@Override
	public void generate(){
		int width=map.length;
		int height=map[0].length;
		var crossroads=new Point(RPG.r(width/4,3*width/4),
				RPG.r(height/4,3*height/4));
		var clear=new HashSet<Point>();
		for(var i=0;i<paths;i++)
			clearpath(crossroads,width,height,clear);
		for(var x=0;x<width;x++)
			for(var y=0;y<height;y++)
				if(!clear.contains(new Point(x,y)))
					map[x][y].blocked=true;
				else if(RPG.random()<obstructed) map[x][y].obstructed=true;
		flood();
	}

	static void clearpath(Point crossroads,int width,int height,
			HashSet<Point> clear){
		var border=new Point(RPG.r(0,width-1),RPG.r(0,height-1));
		if(RPG.chancein(2))
			border.x=RPG.chancein(2)?0:width;
		else
			border.x=RPG.chancein(2)?0:height;
		Walker walker=new Walker(crossroads,border);
		walker.includetarget=true;
		walker.pathing=new DirectPath();
		var extra=2;
		while(RPG.chancein(2))
			extra+=1;
		for(var step:walker.walk()){
			for(var x=step.x-1;x<=step.x+1;x++)
				for(var y=step.y-1;y<=step.y+1;y++){
					var p=new Point(x,y);
					if(p.validate(0,0,width,height)) clear.add(p);
				}
			for(var i=0;i<extra;i++)
				clearextra(step,width,height,clear);
		}
	}

	static void clearextra(Point step,int width,int height,HashSet<Point> clear){
		var extra=new Point(step);
		while(clear.contains(extra))
			extra.displace();
		if(extra.validate(0,0,width,height)) clear.add(extra);
	}
}
