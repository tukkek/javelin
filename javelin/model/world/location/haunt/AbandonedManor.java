package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.view.Images;

/**
 * TODO test pool
 *
 * @author alex
 */
public class AbandonedManor extends Haunt{
	static final List<MonsterType> TYPES=List.of(MonsterType.CONSTRUCT,
			MonsterType.VERMIN,MonsterType.PLANT,MonsterType.MAGICALBEAST);
	static final List<Monster> POOL=Monster.ALL.stream()
			.filter(m->!m.passive&&TYPES.contains(m.type))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.FOREST,Terrain.MARSH,
			Terrain.MOUNTAINS);

	public static class AbandonedManorMap extends LocationMap{
		public AbandonedManorMap(){
			super("Abandoned manor");
			floor=Images.get(List.of("dungeon","floortempleevil"));
			wall=Images.get(List.of("terrain","moldwall"));
			obstacle=Images.get(List.of("terrain","treeforest"));
			flying=false;
		}
	}

	/** Constructor. */
	public AbandonedManor(){
		super("Abandoned manor",AbandonedManorMap.class,POOL,TERRAINS);
	}
}
