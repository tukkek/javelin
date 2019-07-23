package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Haunt for angels, devas and similarly blessed creatures.
 *
 * @author alex
 */
public class HolyGrounds extends Haunt{
	static final List<String> SUBTYPES=List.of("celestial","good","lawful");
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->m.type.equals(MonsterType.OUTSIDER)||include(m,SUBTYPES))
			.filter(m->m.alignment.isgood()).collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.DESERT,Terrain.HILL,
			Terrain.MOUNTAINS,Terrain.PLAIN);

	/** Map. */
	public static class HolyGroundsMap extends LocationMap{
		/** Constructor. */
		public HolyGroundsMap(){
			super("Holy grounds");
			wall=Images.get("terrainrockwall");
			obstacle=Images.get("terrainbush2");
			floor=Images.get("terrainwoodfloor2");
		}

		@Override
		protected Square processtile(Square s,int x,int y,char c){
			if(c=='.')
				s.obstructed=RPG.r(1,3)<=2;
			else if(c=='_'){
				s.blocked=RPG.chancein(8);
				s.obstructed=!s.blocked&&RPG.chancein(6);
			}else if(c=='*')
				s.obstructed=true;
			else
				return super.processtile(s,x,y,c);
			return s;
		}
	}

	/** Constructor. */
	public HolyGrounds(){
		super("Holy grounds",HolyGroundsMap.class,POOL,TERRAINS);
	}
}
