package javelin.model.world.location.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.old.RPG;

/**
 * Helps place {@link Features} in {@link Dungeon}s more smartly than a random
 * naive approach. Mostly takes into consideraton {@link Door}s to divide the
 * map into {@link Zone}s - assuming that the dungeon population would choose to
 * secure their goods, naturally-ocurring features, access to lower levels, etc.
 *
 * @author alex
 */
public class DungeonZoner{
	/**
	 * A {@link Dungeon} might be comprised of one or more zones, with frontiers
	 * represented by doors.
	 *
	 * @author alex
	 */
	public class Zone{
		/** All of the {@link Template#FLOOR} tiles of this zone. */
		public HashSet<Point> area=new HashSet<>();
		/**
		 * All of {@link Door}s of this zone. A door might be represented in
		 * multiple neighboring {@link Zone}s.
		 */
		public HashSet<Door> doors=new HashSet<>();
		/**
		 * A basic representation of how many {@link Door}s a player should need to
		 * cross from the level entrance to reach this zone. Starts at 0.
		 */
		public int depth;

		Zone(Point source,int depth){
			this.depth=depth;
			zones.add(this);
			scan(source);
		}

		void scan(Point p){
			if(!p.validate(0,0,dungeon.size,dungeon.size)||area.contains(p)) return;
			var feature=dungeon.features.get(p.x,p.y);
			if(feature instanceof Door){
				doors.add((Door)feature);
				return;
			}
			if(dungeon.map[p.x][p.y]==Template.WALL) return;
			area.add(p);
			for(var step:getadjacent()){
				step.x+=p.x;
				step.y+=p.y;
				scan(step);
			}
		}

		void expand(){
			var newzones=new ArrayList<Zone>(0);
			for(var door:doors){
				var point=door.getlocation();
				for(var step:getadjacent()){
					step.x+=point.x;
					step.y+=point.y;
					if(step.validate(0,0,dungeon.size,dungeon.size)
							&&dungeon.map[step.x][step.y]!=Template.WALL
							&&!zones.stream().anyMatch(z->z.area.contains(step)))
						newzones.add(new Zone(step,depth+1));
				}
			}
			for(var zone:newzones)
				zone.expand();
		}
	}

	/** All the zones scanned in the given {@link Dungeon}. */
	public List<Zone> zones=new ArrayList<>();
	ArrayList<Point> result=new ArrayList<>();
	Dungeon dungeon;

	/**
	 * @param d Expects a mostly-setup Dungeon (with {@link Door}s, stairs, walls,
	 *          etc), ready to be scanned.
	 */
	public DungeonZoner(Dungeon d,Point entrance){
		dungeon=d;
		new Zone(entrance,0).expand();
		for(var zone:zones)
			for(var point:zone.area)
				for(var i=0;i<Math.pow(2,zone.depth);i++)
					result.add(point);
	}

	/**
	 * @return A random point in the dungeon, with points in a deeper {@link Zone}
	 *         being exponentially more likely to be chosen. Does not take into
	 *         account dungeon size - so a shallow {@link Zone} that is very big
	 *         will have more chance to be represented than a deep {@link Zone}
	 *         that is very small.
	 */
	public Point getpoint(){
		Point p=RPG.pick(result);
		return dungeon.features.get(p.x,p.y)==null?p:getpoint();
	}

	/**
	 * Add randomness so that Chest->Key->Door routes don't become predicatable.
	 */
	static List<Point> getadjacent(){
		List<Point> adjacent=Arrays.asList(Point.getadjacent());
		Collections.shuffle(adjacent);
		return adjacent;
	}

	public void place(Feature f){
		Point p=getpoint();
		f.x=p.x;
		f.y=p.y;
		dungeon.features.add(f);
	}
}
