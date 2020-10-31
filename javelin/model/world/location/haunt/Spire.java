package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Haunt for {@link MonsterType#DRAGON}s.
 *
 * @author alex
 */
public class Spire extends Haunt{
	static final List<Monster> POOL=Monster.ALL.stream()
			.filter(m->m.type.equals(MonsterType.DRAGON))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.MOUNTAINS);

	/** Spire haunt map. */
	public static class SpireMap extends LocationMap{
		/** Constructor. */
		public SpireMap(){
			super("Spire");
			wall=Images.get(List.of("terrain","wall"));
			obstacle=Images.get(List.of("terrain","rock2"));
			floor=Images.get(List.of("terrain","ruggedwall"));
		}

		@Override
		protected Square processtile(Square s,int x,int y,char c){
			if(c=='.')
				s.blocked=true;
			else if(c=='x')
				//clear
				spawnblue.add(new Point(x,y));
			else if(c==' '){
				s.blocked=RPG.chancein(4);
				s.obstructed=!s.blocked&&RPG.chancein(4);
				spawnred.add(new Point(x,y));
			}else if(c=='_'){
				s.blocked=RPG.chancein(6);
				s.obstructed=!s.blocked&&RPG.chancein(2);
			}else
				return super.processtile(s,x,y,c);
			return s;
		}
	}

	/** Constructor. */
	public Spire(){
		super("Dragon spire",SpireMap.class,POOL,TERRAINS);
	}
}
