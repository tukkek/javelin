package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * TODO test and compare to previous pool
 *
 * @author alex
 */
public class OrcSettlement extends Haunt{
	static final List<Terrain> TERRAINS=List.of(Terrain.HILL,Terrain.MARSH,
			Terrain.PLAIN);
	static final List<String> SUBTYPES=List.of("reptilian","goblinoid","orc",
			"gnoll");
	static final List<Monster> POOL=Monster.ALL.stream()
			.filter(m->m.type.equals(MonsterType.HUMANOID)&&include(m,SUBTYPES))
			.collect(Collectors.toList());

	public static class OrcSettlementMap extends LocationMap{
		public OrcSettlementMap(){
			super("Orc settlement");
			wall=Images.get("terrainorcwall");
			flying=false;
		}

		@Override
		public void generate(){
			super.generate();
			for(int x=0;x<map.length;x++)
				for(int y=0;y<map.length;y++)
					if(RPG.chancein(10)&&!nearwall(x,y)) map[x][y].obstructed=true;
		}

		boolean nearwall(int originx,int originy){
			for(int deltax=-1;deltax<=+1;deltax++)
				for(int deltay=-1;deltay<=+1;deltay++){
					int x=originx+deltax;
					int y=originy+deltay;
					if(validate(x,y)&&map[x][y].blocked) return true;
				}
			return false;
		}
	}

	/** Constructor. */
	public OrcSettlement(){
		super("Orc settlement",OrcSettlementMap.class,POOL,TERRAINS);
	}
}
