package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.view.Images;

/**
 * Semi-aquatic haunt, with only a small platform to stand on.
 *
 * @author alex
 */
public class SunkenShip extends Haunt{
	static final List<Monster> POOL=Monster.ALL.stream().filter(m->m.swim>0)
			.collect(Collectors.toList());

	public static class SunkenShipMap extends LocationMap{
		public SunkenShipMap(){
			super("Sunken ship");
			floor=Images.get("terrainshipfloor");
			flooded=Images.get("terrainaquatic");
		}

		@Override
		protected Square processtile(Square tile,int x,int y,char c){
			Square s=super.processtile(tile,x,y,c);
			if(c=='3'){
				s.flooded=true;
				spawnred.add(new Point(x,y));
			}
			return s;
		}
	}

	/** Constructor. */
	public SunkenShip(){
		super("Sunken ship",SunkenShipMap.class,POOL,null);
		allowentry=false;
	}

	boolean nearland(){
		var size=World.scenario.size;
		return new Point(x,y).getadjacent().stream()
				.anyMatch(p->p.validate(0,0,size,size)
						&&!Terrain.get(p.x,p.y).equals(Terrain.WATER));
	}

	@Override
	protected void generate(){
		x=-1;
		while(x==-1||!Terrain.get(x,y).equals(Terrain.WATER)||!nearland())
			generate(this,true);
	}

}
