package javelin.controller.terrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.water.DeepWaters;
import javelin.controller.map.terrain.water.Shore;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Ice;
import javelin.controller.terrain.hazard.Storm;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.ParkedVehicle;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Can only be trespassed by flying or swimming units or boat.
 *
 * TODO water combats
 *
 * TODO water vehicle
 *
 * @author alex
 */
public class Water extends Terrain{
	private Point currentheight;

	/** Constructor. */
	public Water(){
		name="aquatic";
		difficultycap=-3;
		speedtrackless=1f;
		speedroad=1f;
		speedhighway=1f;
		visionbonus=0;
		representation='~';
		liquid=true;
		survivalbonus=-3;
	}

	@Override
	public Maps getmaps(){
		Maps maps=new Maps();
		maps.add(new DeepWaters());
		if(checkshore()) maps.add(new Shore());
		return maps;
	}

	boolean checkshore(){
		if(Squad.active==null) return false;
		Point current=Squad.active.getlocation();
		for(Point p:Point.getadjacent2()){
			p.x+=current.x;
			p.y+=current.y;
			if(p.validate(0,0,World.scenario.size,World.scenario.size)
					&&!Terrain.get(p.x,p.y).equals(Terrain.WATER))
				return true;
		}
		return false;
	}

	@Override
	protected Point generatesource(World world){
		Point source=super.generatesource(world);
		while(!world.map[source.x][source.y].equals(Terrain.MOUNTAINS)
				&&!world.map[source.x][source.y].equals(Terrain.HILL)
				&&search(source,DESERT,World.scenario.desertradius,world)==0)
			source=super.generatesource(world);
		currentheight=source;
		return source;
	}

	@Override
	protected Point expand(HashSet<Point> area,World w){
		Point expand=expand(new ArrayList<>(area),w);
		if(expand==null) throw new RestartWorldGeneration();
		currentheight=expand;
		return currentheight;
	}

	Point expand(ArrayList<Point> pool,World w){
		var visited=new HashSet<>(pool);
		var adjacent=Arrays.asList(Point.getadjacent2());
		for(var point:RPG.shuffle(pool))
			for(var near:RPG.shuffle(adjacent)){
				var candidate=new Point(point.x+near.x,point.y+near.y);
				if(visited.add(candidate)&&verify(candidate,w)) return candidate;
			}
		return null;
	}

	boolean verify(Point p,World w){
		if(checkinvalid(p.x,p.y,w)
				||search(p,DESERT,World.scenario.desertradius,w)>0)
			return false;
		for(Town t:Town.gettowns())
			if(t.distance(p.x,p.y)<=2) return false;
		return true;
	}

	@Override
	protected boolean generatetile(Terrain terrain,World w){
		Terrain current=w.map[currentheight.x][currentheight.y];
		if(terrain.equals(Terrain.MOUNTAINS)&&!current.equals(Terrain.MOUNTAINS))
			return false;
		if(terrain.equals(Terrain.HILL)&&!current.equals(Terrain.MOUNTAINS)
				&&!current.equals(Terrain.HILL))
			return false;
		return true;
	}

	@Override
	protected Point generatereference(Point source,Point current){
		return current;
	}

	@Override
	protected int getareasize(){
		return super.getareasize()/2;
	}

	@Override
	public boolean enter(int x,int y){
		if(Squad.active.swim()) return true;
		Actor a=World.get(x,y);
		if(a==null) return false;
		ParkedVehicle v=a instanceof ParkedVehicle?(ParkedVehicle)a:null;
		if(v!=null&&(v.transport.flies||v.transport.sails)) return true;
		Location l=a instanceof Location?(Location)a:null;
		if(l!=null&&!l.allowentry) return true;
		return false;
	}

	@Override
	public Set<Hazard> gethazards(boolean special){
		Set<Hazard> hazards=super.gethazards(special);
		hazards.add(new Storm());
		if(special) hazards.add(new Ice());
		return hazards;
	}
}