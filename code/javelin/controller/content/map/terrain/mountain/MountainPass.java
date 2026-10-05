package javelin.controller.content.map.terrain.mountain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * Curvy protusions create paths in-between them.
 *
 * @author alex
 */
public class MountainPass extends Map{
	double ratiowalls=RPG.r(25,33)/100.0;
	double ratiotrees=RPG.r(2,10)/100.0;
	Set<Point> walls=new HashSet<>();
	int bouldersize=getarea()/100;

	/** Constructor. */
	public MountainPass(){
		super("Mountain pass",DndMap.SIZE,DndMap.SIZE);
		wall=Images.get(List.of("terrain","rockwall"));
		floor=Images.get(List.of("terrain","towngrass"));
		obstacle=Images.get(List.of("terrain","bush2"));
	}

	@Override
	public void generate(){
		var walltarget=getarea()*ratiowalls;
		while(walls.size()<walltarget)
			buildwalls();
		for(var point:walls)
			if(!RPG.chancein(4)) putwall(point.x,point.y);
		placetrees();
	}

	LinkedList<Point> tracers=new LinkedList<>();

	/** Creates solid walls by generating individual boulders. */
	void buildwalls(){
		var reference=tracers.isEmpty()||RPG.chancein(tracers.size())
				?getrandompoint()
				:RPG.pick(tracers);
		tracers.remove(reference);
		var step=step(reference);
		if(step!=null){
			walls.add(step);
			tracers.add(step);
		}
	}

	Point step(Point reference){
		var near=Point.getadjacentorthogonal();
		for(var p:near){
			p.x+=reference.x;
			p.y+=reference.y;
		}
		var next=Arrays.stream(near)
				.filter(p->validate(p.x,p.y)&&!walls.contains(p))
				.collect(Collectors.toList());
		return next.isEmpty()?null:RPG.pick(next);
	}

	void placetrees(){
		var free=new ArrayList<Point>(getarea()-walls.size());
		for(var x=0;x<map.length;x++)
			for(var y=0;y<map[0].length;y++)
				if(!map[x][y].blocked) free.add(new Point(x,y));
		RPG.shuffle(free);
		var trees=free.size()*ratiotrees;
		for(var i=0;i<trees;i++){
			var tree=free.get(i);
			putobstacle(tree.x,tree.y);
		}
	}
}
