package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;
import tyrant.mikera.engine.RPG;

public class DungeonWorldGenerator extends WorldGenerator {
	public DungeonWorldGenerator() {
		// explicit so reflection can instantiate class
	}

	@Override
	public void generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions, World w) {
		super.generate(realms, regions, w);
		generatenewareas(w);
	}

	HashSet<Point> getfreespace(World w) {
		HashSet<Point> free = new HashSet<Point>(
				World.scenario.size * World.scenario.size);
		int limit = World.scenario.size - 1;
		for (int x = 0; x < World.scenario.size; x++) {
			for (int y = 0; y < World.scenario.size; y++) {
				if (x == 0 || y == 0 || x == limit || y == limit) {
					continue;
				}
				if (w.map[x][y] == Terrain.FOREST) {
					free.add(new Point(x, y));
				}
			}
		}
		return free;
	}

	void generatenewareas(World w) {
		int newareas = 3 + RPG.r(1, 4);
		List<Terrain> terrains = new ArrayList<Terrain>(
				Arrays.asList(Terrain.ALL));
		terrains.remove(Terrain.WATER);
		terrains.remove(Terrain.UNDERGROUND);
		HashSet<Point> free = getfreespace(w);
		int size = free.size() / (newareas + 1);
		for (int i = 0; i < newareas; i++) {
			generatenewarea(RPG.pick(terrains), free, size, w);
		}
	}

	void generatenewarea(Terrain t, HashSet<Point> region, int size,
			World w) {
		if (t == Terrain.FOREST) {
			return;
		}
		size += RPG.randomize(size) - 1;
		Point source = RPG.pick(new ArrayList<Point>(region));
		ArrayList<Point> newarea = new ArrayList<Point>(size);
		newarea.add(source);
		while (size > 0) {
			newarea.add(expand(t, newarea, region));
			size -= 1;
		}
		int limit = World.scenario.size - 1;
		for (Point p : newarea) {
			if (p.x != 0 && p.y != 0 && p.x != limit && p.y != limit) {
				w.map[p.x][p.y] = t;
			}
		}
	}

	Point expand(Terrain t, List<Point> pool, HashSet<Point> region) {
		int[] steps = new int[] { -1, 0, +1 };
		Point p = new Point(RPG.pick(pool));
		while (pool.contains(p)) {
			p.x += RPG.pick(steps);
			p.y += RPG.pick(steps);
			if (!region.contains(p)) {
				p = new Point(RPG.pick(pool));
				WorldGenerator.retry();
			}
		}
		return p;
	}
}