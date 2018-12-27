package javelin.controller.map.terrain.mountain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.model.world.location.haunt.Haunt;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * A twisted city of lanes, alleys and structures.
 *
 * TODO better suited as a high-ranking cold-based {@link Haunt}
 *
 * @author alex
 */
public class MountainsOfMadness extends Map{
	static final List<Integer> STRUCTURESIZE=List.of(2,4,8);
	static final float AREA=DndMap.SIZE*DndMap.SIZE;
	static final List<Point> DIRECTIONS=List.of(new Point(+1,0),new Point(-1,0),
			new Point(0,+1),new Point(0,-1));

	Set<Point> structures=new HashSet<>();
	float structureratio=RPG.r(10,20)/100f;
	Set<Point> maze=new HashSet<>();
	float mazeratio=RPG.r(20,30)/100f;
	float mazeclear=.1f;

	/** Default constructor. */
	public MountainsOfMadness(){
		super("Mountains of madness",DndMap.SIZE,DndMap.SIZE);
		floor=Images.get("terrainice");
	}

	@Override
	public void generate(){
		while(structures.size()<AREA*structureratio)
			buildstructure();
		while(maze.size()<(AREA-structures.size())*mazeratio)
			buildmaze();
		clearmaze();
	}

	static Point getrandomtile(){
		return new Point(RPG.r(0,DndMap.SIZE-1),RPG.r(0,DndMap.SIZE-1));
	}

	void buildstructure(){
		var width=RPG.pick(STRUCTURESIZE);
		var height=RPG.pick(STRUCTURESIZE);
		var from=getrandomtile();
		from.x+=RPG.randomize(width);
		from.y+=RPG.randomize(height);
		var empty=RPG.chancein(4);
		for(var x=from.x;x<=from.x+width;x++)
			for(var y=from.y;y<=from.y+height;y++){
				var tile=new Point(x,y);
				if(tile.validate(0,0,DndMap.SIZE,DndMap.SIZE)){
					map[x][y].blocked=!empty;
					structures.add(tile);
				}
			}
	}

	void buildmaze(){
		var p=getrandomtile();
		while(!structures.contains(p)&&p.validate(0,0,DndMap.SIZE,DndMap.SIZE)){
			var d=RPG.pick(DIRECTIONS);
			for(var i=RPG.pick(STRUCTURESIZE);i>0;i--){
				map[p.x][p.y].blocked=true;
				maze.add(new Point(p));
				p.x+=d.x;
				p.y+=d.y;
				if(structures.contains(p)||!p.validate(0,0,DndMap.SIZE,DndMap.SIZE))
					return;
			}
		}
	}

	void clearmaze(){
		var clear=maze.size()*mazeclear;
		var maze=RPG.shuffle(new ArrayList<>(this.maze));
		for(int i=0;i<clear;i++){
			var p=maze.get(i);
			map[p.x][p.y].blocked=false;
		}
	}
}
