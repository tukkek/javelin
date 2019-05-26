package javelin.controller.scenario.dungeondelve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javelin.controller.Point;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;

public class DungeonDelveGenerator extends FeatureGenerator{
	public final Map<Integer,Dungeon> dungeons=new TreeMap<>();

	@Override
	public Location generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		Dungeon parent=null;
		for(int floor=1;floor<=DungeonDelve.FLOORS;floor++){
			var d=new Megadungeon(floor,floor,parent);
			dungeons.put(floor,d);
			if(floor==1){
				d.x=World.scenario.size/2;
				d.y=d.x;
				d.place();
			}
			parent=d;
		}
		return dungeons.get(1);
	}

	@Override
	public void spawn(float chance,boolean generatingworld){
		//don't
	}
}
