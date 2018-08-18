package javelin.controller.map.terrain.underground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.old.RPG;
import javelin.old.underground.Caves;

/**
 * Builds something similar to what you'd get placing printed dungeon tiles
 * together.
 *
 * @author alex
 */
public class Constructed extends Caves{
	/** Constructor. */
	public Constructed(){
		super("Constructed");
	}

	@Override
	public void generate(){
		List<Set<Point>> blocks=new ArrayList<>();
		Set<Point> occupied=new HashSet<>();
		defineblocks(blocks,occupied);
		for(Set<Point> block:blocks)
			if(block.size()==1) occupied.removeAll(block);
		for(Point p:occupied)
			map[p.x][p.y].blocked=true;
		closeborders();
		placeobstacles();
	}

	void defineblocks(List<Set<Point>> blocks,Set<Point> occupied){
		int tiles=map.length*map[0].length;
		int target=tiles/3;
		int nblocks=target/5;
		while(blocks.size()<nblocks){
			HashSet<Point> block=new HashSet<>();
			Point starting=new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
			block.add(starting);
			occupied.add(starting);
			blocks.add(block);
		}
		while(occupied.size()<target){
			Set<Point> block=RPG.pick(blocks);
			Point current=RPG.pick(new ArrayList<>(block));
			Point expand=new Point(current);
			if(RPG.chancein(2))
				expand.x+=RPG.chancein(2)?+1:-1;
			else
				expand.y+=RPG.chancein(2)?+1:-1;
			if(!expand.validate(0,0,map.length,map[0].length)
					||occupied.contains(expand)||checkblocking(expand,occupied))
				continue;
			block.add(expand);
			occupied.add(expand);
		}
	}

	void placeobstacles(){
		ArrayList<Point> free=new ArrayList<>();
		for(int x=0;x<map.length;x++)
			for(int y=0;y<map[0].length;y++)
				if(!map[x][y].blocked) free.add(new Point(x,y));
		Collections.shuffle(free);
		int target=Math.round(free.size()*RPG.r(1,10)/100f);
		for(int i=0;i<target;i++){
			Point p=free.get(i);
			map[p.x][p.y].obstructed=true;
		}
	}

	void closeborders(){
		for(int x=0;x<map.length;x++)
			for(int y=0;y<map[0].length;y++)
				if(x==0||x==map.length-1||y==0||y==map[0].length-1)
					map[x][y].blocked=true;
	}

	boolean checkblocking(Point expand,Set<Point> occupied){
		if(Javelin.DEBUG) return false;
		for(Point p:Point.getadjacentorthogonal()){
			p.x+=expand.x;
			p.y+=expand.y;
			if(!occupied.contains(p)) return false;
		}
		return true;
	}
}
