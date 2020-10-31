package javelin.controller.map.terrain.mountain;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * TODO select better terrain tiles
 *
 * @author alex
 */
public class MountainCave extends Map{
	static final int SIZE=DndMap.SIZE;
	static final Image TREE=Images.get(List.of("terrain","tree"));
	static final Image GRASS=Images.get(List.of("terrain","towngrass"));

	Set<Point> mountain=new HashSet<>();
	Set<Point> outside=new HashSet<>();

	public MountainCave(){
		super("Mountain cave",SIZE,SIZE);
		floor=Images.get(List.of("terrain","dirt"));
		wall=Images.get(List.of("terrain","ruggedwall"));
		obstacle=Images.get(List.of("terrain","rock3"));
	}

	void buildwall(){
		var wall=new ArrayList<Point>(SIZE);
		var start=RPG.r(SIZE/10,SIZE/2);
		var end=RPG.r(SIZE/10,SIZE/2);
		for(var y=0;y<SIZE;y++)
			wall.add(new Point(start+y*(end-start)/SIZE,y));
		for(var w:wall){
			if(RPG.chancein(2)) w.x+=RPG.chancein(2)?+1:-1;
			if(w.x<0)
				w.x=0;
			else if(w.x>=SIZE) w.x=SIZE-1;
			for(var x=w.x;x<SIZE;x++)
				mountain.add(new Point(x,w.y));
		}
	}

	Point grow(Set<Point> area){
		Point p=null;
		while(p==null||!p.validate(1,1,SIZE-1,SIZE-1)){
			p=new Point(RPG.pick(area));
			p.displaceaxis();
		}
		area.add(p);
		return p;
	}

	Set<Point> carve(Point seed){
		var area=new HashSet<Point>();
		area.add(seed);
		var p=seed;
		while(map[p.x][p.y].blocked)
			p=grow(area);
		area.forEach(t->map[t.x][t.y].blocked=false);
		return area;
	}

	void carvetunnels(){
		var mountain=new HashSet<>(this.mountain);
		var ntunnels=RPG.rolldice(4,4);
		var tunnels=new HashSet<Point>(ntunnels);
		while(tunnels.size()<ntunnels)
			tunnels.add(RPG.pick(mountain));
		var outside=RPG.pick(this.outside);
		var entrance=tunnels.stream()
				.min((a,b)->Double.compare(a.distance(outside),b.distance(outside)))
				.orElseThrow();
		tunnels.clear();
		tunnels.addAll(carve(entrance));
		mountain.removeAll(tunnels);
		for(var i=1;i<=ntunnels;i++){
			Point seed=null;
			while(seed==null||seed.distanceinsteps(RPG.pick(tunnels))>12)
				seed=RPG.pick(mountain);
			carve(seed);
		}
	}

	void placeobstacles(Set<Point> area,int min,int max){
		var clear=new ArrayList<Point>(area.size());
		for(var p:area)
			if(!map[p.x][p.y].blocked) clear.add(p);
		for(var p:RPG.shuffle(clear).subList(0,area.size()/RPG.r(min,max)))
			map[p.x][p.y].obstructed=true;
	}

	/** Change coordinates so it's not the same axis every time. */
	void tilt(Set<Point> area){
		var original=new ArrayList<>(area);
		area.clear();
		var invertx=RPG.chancein(2);
		var inverty=RPG.chancein(2);
		var translate=RPG.chancein(2);
		for(var p:original){
			if(invertx) p.x=SIZE-1-p.x;
			if(inverty) p.y=SIZE-1-p.y;
			if(translate) p=new Point(p.y,p.x);
			area.add(p);
		}
	}

	@Override
	public void generate(){
		buildwall();
		tilt(mountain);
		mountain.forEach(p->map[p.x][p.y].blocked=true);
		for(var x=0;x<SIZE;x++)
			for(var y=0;y<SIZE;y++){
				var p=new Point(x,y);
				if(!mountain.contains(p)) outside.add(p);
			}
		//		tilt(outside);
		placeobstacles(outside,6,10);
		carvetunnels();
		var inside=new HashSet<Point>(mountain.size());
		for(var p:mountain)
			if(!map[p.x][p.y].blocked) inside.add(p);
		placeobstacles(inside,10,20);
	}

	@Override
	public Image getobstacle(int x,int y){
		return mountain.contains(new Point(x,y))?obstacle:TREE;
	}

	@Override
	public Image getfloor(int x,int y){
		return mountain.contains(new Point(x,y))?floor:GRASS;
	}
}
