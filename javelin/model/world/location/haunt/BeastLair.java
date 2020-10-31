package javelin.model.world.location.haunt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * Forest grove for beast, magical beasts and feys.
 *
 * @author alex
 */
public class BeastLair extends Haunt{
	static final List<MonsterType> TYPES=List.of(MonsterType.ANIMAL,
			MonsterType.MAGICALBEAST,MonsterType.FEY);
	static final List<String> SUBTYPES=List.of("animal","dire animal");
	static final List<Monster> POOL=Monster.ALL.stream()
			.filter(m->TYPES.contains(m.type)||include(m,SUBTYPES))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.FOREST);

	/** Beast lair map. */
	public static class LairMap extends LocationMap{
		Set<Point> lake=new HashSet<>();

		/** Constructor. */
		public LairMap(){
			super("Beast lair");
			obstacle=Images.get(List.of("terrain","bush2"));
			wall=Images.get(List.of("terrain","treeforest"));
			flooded=Images.get(List.of("terrain","aquatic"));
			floor=Images.get(List.of("terrain","forestfloor"));
		}

		@Override
		protected Square processtile(Square s,int x,int y,char c){
			if(c=='.')
				s.obstructed=RPG.r(1,4)>=2;
			else if(c=='~'){
				s.flooded=!RPG.chancein(10);
				lake.add(new Point(x,y));
			}else if(c=='T'||c=='t')
				s.blocked=RPG.chancein(3);
			else if(c==' '){
				s.blocked=RPG.chancein(6);
				if(!s.blocked){
					spawnred.add(new Point(x,y));
					s.obstructed=RPG.r(1,4)>=2;
				}
			}else
				return super.processtile(s,x,y,c);
			return s;
		}

		void flood(int x,int y){
			if(validate(x,y)){
				map[x][y].clear();
				map[x][y].flooded=true;
			}
		}

		void makeriver(){
			var start=RPG.pick(lake);
			var mid=new Point(map.length/2,map[0].length/2);
			var horizontal=List.of(0,start.x>mid.x?+1:-1);
			var vertical=List.of(0,start.y>mid.y?+1:-1);
			var width=RPG.r(0,2);
			var river=start;
			while(validate(river.x,river.y)){
				river.x+=RPG.pick(horizontal);
				river.y+=RPG.pick(vertical);
				for(var x=river.x-width;x<=river.x+width;x++)
					for(var y=river.y-width;y<=river.y+width;y++)
						flood(x,y);
			}
		}

		@Override
		public void generate(){
			super.generate();
			var rivers=RPG.r(1,4);
			for(var i=0;i<rivers;i++)
				makeriver();
		}
	}

	/** Constructor. */
	public BeastLair(){
		super("Beast lair",LairMap.class,POOL,TERRAINS);
	}
}
