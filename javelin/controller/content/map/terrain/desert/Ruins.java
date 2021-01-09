package javelin.controller.content.map.terrain.desert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Desert;
import javelin.controller.content.terrain.Forest;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * {@link Desert} ruins - technically could be {@link Forest} or anything else
 * but deserts being mostly empty with a lot of open space fit better
 * conceptually.
 *
 * TODO could be used as a {@link Haunt}
 *
 * @author alex
 */
public class Ruins extends Map{
	interface Step{
		void generate(Ruins r);
	}

	static final Step DEBRIS=(r)->{
		var empty=new ArrayList<>(r.getallempty());
		var amount=RPG.r(empty.size()/20,empty.size()/12);
		for(var e:RPG.shuffle(empty).subList(0,amount))
			r.map[e.x][e.y].blocked=true;
	};

	static final Step OBSTACLES=(r)->{
		var empty=new ArrayList<>(r.getallempty());
		var amount=RPG.r(empty.size()/20,empty.size()/12);
		for(var e:RPG.shuffle(empty).subList(0,amount))
			r.map[e.x][e.y].obstructed=true;
	};

	static final Step WALLS=(r)->{
		var amount=RPG.r(10,20);
		for(var wall=0;wall<amount;wall++){
			var origin=RPG.pick(r.getallempty());
			var x=0;
			var y=0;
			if(RPG.chancein(2))
				x=RPG.chancein(2)?+1:-1;
			else
				y=RPG.chancein(2)?+1:-1;
			var length=RPG.randomize(5,3,Integer.MAX_VALUE);
			for(int i=0;i<length&&r.validate(origin.x,origin.y);i++){
				r.map[origin.x][origin.y].blocked=true;
				origin.x+=x;
				origin.y+=y;
			}
		}
	};

	static final Step STRUCTURES=(r)->{
		var amount=RPG.r(4,8);
		for(var i=0;i<amount;i++)
			makestructure(r,RPG.r(1,6)+1,RPG.r(1,6)+1);
	};

	static final Step LARGESTRUCTURES=(r)->{
		var amount=RPG.r(1,4);
		for(var i=0;i<amount;i++)
			makestructure(r,RPG.r(8,10),RPG.r(8,10));
	};

	static final Step PATH=(r)->{
		var width=RPG.r(1,4);
		var clear=new HashSet<Point>();
		var path=new HashSet<Point>();
		var horizontal=RPG.chancein(2);
		if(horizontal){
			var origin=RPG.r(0,r.map[0].length-width-2);
			clear.addAll(Point.getrange(0,origin,r.map.length,origin+width+2));
			path.addAll(Point.getrange(0,origin,r.map.length,origin+1));
			path.addAll(Point.getrange(0,origin+width+1,r.map.length,origin+width+2));
		}else{
			var origin=RPG.r(0,r.map.length-width-2);
			clear.addAll(Point.getrange(origin,0,origin+width+2,r.map[0].length));
			path.addAll(Point.getrange(origin,0,origin+1,r.map[0].length));
			path.addAll(
					Point.getrange(origin+width+1,0,origin+width+2,r.map[0].length));
		}
		for(var c:clear)
			r.map[c.x][c.y].clear();
		for(var p:path){
			var tile=r.map[p.x][p.y];
			if(RPG.chancein(2))
				tile.blocked=true;
			else
				tile.obstructed=RPG.chancein(4);
		}
	};

	static final Step TEAR=(r)->{
		var wall=Point.getrange(0,0,r.map.length,r.map[0].length).stream()
				.map(p->r.map[p.x][p.y]).filter(s->s.blocked)
				.collect(Collectors.toList());
		for(var w:wall)
			if(RPG.chancein(2)){
				w.blocked=false;
				w.obstructed=RPG.chancein(2);
			}
	};

	static final Step HOLLOW=(r)->{
		var hollow=new HashSet<Point>();
		for(var p:Point.getrange(0,0,r.map.length,r.map[0].length)){
			var walls=0;
			for(var a:p.getadjacent())
				if(r.validate(a.x,a.y)&&r.map[a.x][a.y].blocked) walls+=1;
			if(walls>=6) hollow.add(p);
		}
		for(var h:hollow)
			r.map[h.x][h.y].blocked=false;
	};

	static final List<Step> STEPS=new ArrayList<>(List.of(DEBRIS,OBSTACLES,WALLS,
			STRUCTURES,LARGESTRUCTURES,HOLLOW,PATH,TEAR));
	static final List<Step> LAST=List.of(PATH,HOLLOW,TEAR);

	/** Constructor. */
	public Ruins(){
		super("ruins",DndMap.SIZE,DndMap.SIZE);
		floor=Images.get(List.of("terrain","desert"));
		wall=Images.get(List.of("terrain","orcwall"));
		obstacle=Images.get(List.of("terrain","rock"));
	}

	static void makestructure(Ruins r,int width,int depth){
		var origin=RPG.pick(r.getallempty());
		for(var p:Point.getrange(origin.x,origin.y,origin.x+width,origin.y+depth))
			if(r.validate(p.x,p.y)&&!RPG.chancein(6)) r.map[p.x][p.y].blocked=true;
	}

	@Override
	public void generate(){
		var steps=new HashSet<Step>(4);
		for(var i=0;i<5;i++)
			steps.add(RPG.pick(STEPS));
		var last=new ArrayList<>(LAST);
		last.retainAll(steps);
		steps.removeAll(last);
		for(var s:RPG.shuffle(new ArrayList<>(steps)))
			s.generate(this);
		for(var l:last)
			l.generate(this);
	}
}
