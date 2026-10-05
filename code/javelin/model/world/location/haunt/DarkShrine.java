package javelin.model.world.location.haunt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.content.map.location.LocationMap;
import javelin.controller.content.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Haunt for evil {@link MonsterType#OUTSIDER}s, demons, devils, etc.
 *
 * @author alex
 */
public class DarkShrine extends Haunt{

	static final List<String> SUBTYPES=List.of("demon (tanar'ri)","devil","evil");
	static final List<Monster> POOL=Monster.ALL
			.stream().filter(m->m.type.equals(MonsterType.OUTSIDER)
					&&m.alignment.isevil()&&include(m,SUBTYPES))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.FOREST,Terrain.MARSH);

	/** Dark shrine map. */
	public static class ShrineMap extends LocationMap{
		Set<Point> area=new HashSet<>();
		Set<Point> seeds=new HashSet<>();
		Set<Point> blocked=new HashSet<>();

		/** Constructor. */
		public ShrineMap(){
			super("Dark shrine");
			wall=Images.get(List.of("terrain","orcwall"));
			floor=Images.get(List.of("terrain","traininghall"));
		}

		@Override
		protected Square processtile(Square s,int x,int y,char c){
			if(c=='1')
				spawnblue.add(new Point(x,y));
			else if(c=='2')
				spawnred.add(new Point(x,y));
			else if(c==' ')
				area.add(new Point(x,y));
			else
				return super.processtile(s,x,y,c);
			return s;
		}

		@Override
		public void generate(){
			super.generate();
			var nseeds=RPG.rolldice(10,4);
			while(seeds.size()<nseeds)
				seeds.add(RPG.pick(area));
			var target=area.size()*1.0/RPG.r(2,3);
			while(blocked.size()<target){
				var seed=RPG.pick(seeds);
				var move=new Point(seed);
				move.displace();
				if(!validate(move.x,move.y)||!area.contains(move)) continue;
				blocked.add(move);
				map[move.x][move.y].blocked=true;
				seed.x=move.x;
				seed.y=move.y;
			}
		}
	}

	/** Constructor. */
	public DarkShrine(){
		super("Dark shrine",ShrineMap.class,POOL,TERRAINS);
	}
}
