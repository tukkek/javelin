package javelin.old.underground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.old.RPG;
import javelin.view.Images;

public class Complex extends Map{
	static final double SEED=.1;
	HashMap<Point,ArrayList<Point>> sections=new HashMap<>();
	int occupied=0;
	int area;

	public Complex(){
		super("Underground complex",DndMap.SIZE,DndMap.SIZE);
		floor=Images.get(List.of("terrain","dungeonfloor"));
		wall=Images.get(List.of("terrain","dungeonwall"));
		area=map.length*map[0].length;
		flying=false;
	}

	@Override
	public void generate(){
		while(occupied/(float)area<SEED)
			generateseed();
		double target=RPG.r(25,50)/100f;
		ArrayList<Point> seeds=new ArrayList<>(sections.keySet());
		while(occupied/(float)area<target)
			expand(sections.get(RPG.pick(seeds)));
		for(Point seed:seeds)
			for(Point p:sections.get(seed))
				map[p.x][p.y].blocked=true;
	}

	void expand(ArrayList<Point> section){
		Point expand=new Point(RPG.pick(section));
		int increment=RPG.chancein(2)?+1:-1;
		if(RPG.chancein(2))
			expand.x+=increment;
		else
			expand.y+=increment;
		if(expand.validate(0,0,map.length,map[0].length)){
			section.add(expand);
			occupied+=1;
		}
	}

	void generateseed(){
		Point seed=new Point(RPG.r(0,map.length-1),RPG.r(0,map[0].length-1));
		if(sections.containsKey(seed)) return;
		ArrayList<Point> walls=new ArrayList<>();
		walls.add(seed);
		sections.put(seed,walls);
		occupied+=1;
	}
}
