package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.world.Actor;
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
			floor=Images.get(List.of("terrain","shipfloor"));
			flooded=Images.get(List.of("terrain","aquatic"));
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
		super("Sunken ship",SunkenShipMap.class,POOL,List.of(Terrain.WATER));
		allowentry=false;
	}

	@Override
	protected boolean validateplacement(boolean water,World w,List<Actor> actors){
		return Terrain.get(x,y).equals(Terrain.WATER)
				&&Terrain.search(getlocation(),Terrain.WATER,1,w)>0
				&&super.validateplacement(water,w,actors);
	}

	@Override
	protected void generate(boolean water){
		super.generate(true);
	}
}
