package javelin.model.world.location.haunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.state.Square;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * {@link Haunt} with {@link MonsterType#ELEMENTAL}s and mephits.
 *
 * @author alex
 */
public class Conflux extends Haunt{
	static final List<String> SUBTYPES=List.of("mephit");
	static final List<Monster> POOL=Monster.ALL.stream()
			.filter(m->m.type.equals(MonsterType.ELEMENTAL)||include(m,SUBTYPES))
			.filter(m->m.walk>0||m.fly>0||m.burrow>0).collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.DESERT,Terrain.MOUNTAINS,
			Terrain.FOREST,Terrain.MARSH);

	/** Map for Conflux haunt. */
	public static class ConfluxMap extends LocationMap{
		Set<Point> lake=new HashSet<>();
		int bushchance=RPG.pick(List.of(2,4,6));
		List<Square> blue;
		List<Square> red;

		/** Constructor. */
		public ConfluxMap(){
			super("Conflux");
			floor=Images.get("terraintowngrass");
			wall=Images.get("terrainwall");
			obstacle=Images.get("terrainbush");
		}

		@Override
		protected Square processtile(Square s,int x,int y,char c){
			s.obstructed=RPG.chancein(bushchance);
			return s; //don't
		}

		List<Point> drawline(Point p,int deltax,int deltay){
			var line=new ArrayList<Point>();
			var delta=Math.min(p.x,p.y);
			p.x-=delta;
			p.y-=delta;
			while(validate(p.x,p.y)){
				line.add(p);
				p=new Point(p);
				if(RPG.chancein(2)) p.x+=deltax;
				if(RPG.chancein(2)) p.y+=deltay;
			}
			return line;
		}

		@Override
		public void generate(){
			super.generate();
			draw();
			if(RPG.chancein(2)){
				var m=Arrays.asList(map);
				Collections.reverse(m);
				map=m.toArray(map);
			}
			if(RPG.chancein(2)||true) for(int i=0;i<map.length;i++){
				var m=Arrays.asList(map[i]);
				Collections.reverse(m);
				map[i]=m.toArray(map[i]);
			}
			for(int x=0;x<map.length;x++)
				for(int y=0;y<map[x].length;y++)
					if(blue.contains(map[x][y]))
						spawnblue.add(new Point(x,y));
					else if(red.contains(map[x][y])) spawnred.add(new Point(x,y));
		}

		void draw(){
			var lines=new LinkedList<List<Point>>();
			var from=0;
			var width=map.length-1;
			while(validate(width-from,from)){
				lines.add(drawline(new Point(width-from,from),+1,+1));
				from+=RPG.r(1,4)+1;
			}
			for(var p:lines.pollLast())
				while(validate(p.x,p.y)){
					map[p.x][p.y].clear();
					map[p.x][p.y].flooded=true;
					lake.add(new Point(p));
					p.y+=1;
				}
			var spawn=new LinkedList<>(List.of(lines.pop(),lines.pollLast()));
			blue=RPG.shuffle(spawn).pop().stream().map(p->map[p.x][p.y])
					.collect(Collectors.toList());
			red=spawn.pop().stream().map(p->map[p.x][p.y])
					.collect(Collectors.toList());
			for(var line:lines)
				for(var p:line){
					map[p.x][p.y].clear();
					map[p.x][p.y].blocked=RPG.r(1,3)>=2;
				}
			var rivers=RPG.r(1,4);
			for(var i=0;i<rivers;i++)
				makeriver(RPG.pick(lake));
		}

		void makeriver(Point from){
			var to=new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
			if(RPG.chancein(2))
				to.x=map.length-1;
			else
				to.y=0;
			Walker walker=new Walker(from,to);
			walker.pathing=new DirectPath();
			walker.includetarget=true;
			for(var p:walker.walk()){
				if(!lake.contains(p)&&map[p.x][p.y].flooded) break;
				map[p.x][p.y].clear();
				map[p.x][p.y].flooded=true;
			}
		}
	}

	/** Constructor. */
	public Conflux(){
		super("Elemental conflux",ConfluxMap.class,POOL,TERRAINS);
	}
}
