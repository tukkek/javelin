package javelin.controller.map.terrain.forest;

import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.old.RPG;
import javelin.view.Images;

public class ForestPath extends Map{
	float obstructoin=RPG.r(0,30)/100f;

	/** Constructor. */
	public ForestPath(){
		super("Forest path",DndMap.SIZE,DndMap.SIZE);
		Medium.standarize(this);
		obstacle=Images.get("terrainbush2");
	}

	@Override
	public void generate(){
		int width=map.length;
		int height=map[0].length;
		var crossroads=new Point(RPG.r(width/4,3*width/4),
				RPG.r(height/4,3*height/4));
		int paths=RPG.r(1,4)+1;
		var clear=new HashSet<Point>();
		for(var i=0;i<paths;i++)
			clearpath(crossroads,width,height,clear);
		for(var x=0;x<width;x++)
			for(var y=0;y<height;y++)
				if(!clear.contains(new Point(x,y)))
					map[x][y].blocked=true;
				else if(RPG.random()<obstructoin) map[x][y].obstructed=true;
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
