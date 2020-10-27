package javelin.model.world.location.haunt;

import java.util.ArrayList;
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
 * TODO will probably need to review {@link #TYPES} after remaining haunts
 * included.
 *
 * @author alex
 */
public class ShatteredTemple extends Haunt{
	static final List<String> SUBTYPES=List.of("chaotic","good","evil","lawful");
	static final List<Monster> POOL=new ArrayList<>(Monster.ALL.stream()
			.filter(m->m.type.equals(MonsterType.OUTSIDER)||include(m,SUBTYPES))
			.collect(Collectors.toList()));
	static final List<Terrain> TERRAINS=List.of(Terrain.DESERT,Terrain.FOREST,
			Terrain.HILL,Terrain.MARSH);

	static{
		POOL.removeAll(HolyGrounds.POOL);
		POOL.removeAll(Conflux.POOL);
		POOL.removeAll(DarkShrine.POOL);
	}

	public static class ShatteredTempleMap extends LocationMap{
		public ShatteredTempleMap(){
			super("Shattered temple");
			floor=Images.get("dungeonfloortempleevil");
			wall=Images.get("terrainrockwall2");
			obstacle=Images.get("terrainbush");
		}

		@Override
		protected Square processtile(Square tile,int x,int y,char c){
			Square s=super.processtile(tile,x,y,c);
			if(!s.blocked&&RPG.r(1,6)==1) s.obstructed=true;
			return s;
		}
	}

	/** Constructor. */
	public ShatteredTemple(){
		super("Shaterred temple",ShatteredTempleMap.class,POOL,TERRAINS);
	}
}
