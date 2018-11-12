package javelin.controller.scenario.dungeondelve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;

public class DungeonDelveWorld extends WorldGenerator{
	@Override
	protected void generategeography(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions,World w){
		for(int i=0;i<World.scenario.size;i++)
			for(int j=0;j<World.scenario.size;j++)
				w.map[i][j]=Terrain.FOREST;
	}
}
