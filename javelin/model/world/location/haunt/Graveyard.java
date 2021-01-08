package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.content.map.location.LocationMap;
import javelin.controller.content.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * {@link MonsterType#UNDEAD} haunt.
 *
 * @author alex
 */
public class Graveyard extends Haunt{
	static final List<Monster> POOL=Monster.ALL.stream()
			.filter(m->m.type.equals(MonsterType.UNDEAD))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.FOREST,Terrain.HILL,
			Terrain.MOUNTAINS,Terrain.PLAIN);

	public static class GraveyardMap extends LocationMap{
		public GraveyardMap(){
			super("graveyard");
			wall=Images.get(List.of("terrain","tombstone"));
			obstacle=Images.get(List.of("terrain","bush"));
			floor=Images.get(List.of("dungeon","floortempleevil"));
		}

		@Override
		protected Square processtile(Square tile,int x,int y,char c){
			Square s=super.processtile(tile,x,y,c);
			if(!s.blocked&&RPG.chancein(20)) s.obstructed=true;
			return s;
		}
	}

	/** Constructor. */
	public Graveyard(){
		super("Graveyeard",GraveyardMap.class,POOL,TERRAINS);
	}
}
