package javelin.model.world.location.haunt;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.SunkenShipMap;
import javelin.controller.terrain.Terrain;
import javelin.model.world.World;

/**
 * Semi-aquatic haunt, with only a small platform to stand on.
 *
 * @author alex
 */
public class SunkenShip extends Haunt{
	/** Constructor. */
	public SunkenShip(){
		super("Sunken ship",5,10,new String[]{"Aquatic elf","merfolk","Locathah",
				"octopus","Skum","Sahuagin","Malenti","Sahuagin mutant"});
	}

	@Override
	public LocationMap getmap(){
		return new SunkenShipMap();
	}

	@Override
	protected void generate(){
		x=-1;
		while(x==-1||!Terrain.get(x,y).equals(Terrain.WATER)||!nearland())
			generate(this,true);
	}

	boolean nearland(){
		var size=World.scenario.size;
		return new Point(x,y).getadjacent().stream()
				.anyMatch(p->p.validate(0,0,size,size)
						&&!Terrain.get(p.x,p.y).equals(Terrain.WATER));
	}
}
