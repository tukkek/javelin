package javelin.model.world;

import java.util.ArrayList;
import java.util.List;

import javelin.JavelinApp;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

public class Dungeon implements WorldActor {
	public int x = -1;
	public int y;
	public Thing visual = null;
	public static List<Dungeon> dungeons = new ArrayList<Dungeon>();

	public Dungeon() {
		generate();
		dungeons.add(this);
	}

	public void generate() {
		while (x == -1 || WorldMap.isoccupied(x, y, true)) {
			x = RPG.r(0, WorldMap.MAPDIMENSION - 1);
			y = RPG.r(0, WorldMap.MAPDIMENSION - 1);
		}
	}

	public static Dungeon isdungeon(final int x2, final int y2) {
		for (final Dungeon d : dungeons) {
			if (x2 == d.x && y2 == d.y) {
				return d;
			}
		}
		return null;
	}

	@Override
	public int getx() {
		return x;
	}

	@Override
	public int gety() {
		return y;
	}

	@Override
	public void remove() {
		dungeons.remove(this);
		visual.remove();
	}

	@Override
	public void place() {
		visual = Lib.create("dungeon");
		JavelinApp.overviewmap.addThing(visual, x, y);

	}

	@Override
	public String describe() {
		return "a lair";
	}

}
