package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.old.RPG;

public class DungeonWorldGenerator extends WorldGenerator {
	@Override
	public void generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions, World w) {
		super.generate(realms, regions, w);
		generatenewareas(w);
	}

	HashSet<Point> getfreespace(World w) {
		HashSet<Point> free = new HashSet<>(
				World.scenario.size * World.scenario.size);
		int limit = World.scenario.size - 1;
		for (int x = 0; x < World.scenario.size; x++) {
			for (int y = 0; y < World.scenario.size; y++) {
				if (x == 0 || y == 0 || x == limit || y == limit) {
					continue;
				}
				Terrain t = w.map[x][y];
				if (t == Terrain.WATER) {
					w.map[x][y] = Terrain.FOREST;
					free.add(new Point(x, y));
				} else if (t == Terrain.FOREST) {
					free.add(new Point(x, y));
				}
			}
		}
		return free;
	}

	void generatenewareas(World w) {
		int newareas = 3 + RPG.r(1, 4);
		List<Terrain> terrains = new ArrayList<>(
				Arrays.asList(Terrain.ALL));
		terrains.remove(Terrain.WATER);
		terrains.remove(Terrain.UNDERGROUND);
		HashSet<Point> free = getfreespace(w);
		int size = free.size() / (newareas + 1);
		for (int i = 0; i < newareas; i++) {
			generatenewarea(RPG.pick(terrains), free, size, w);
		}
	}

	void generatenewarea(Terrain t, HashSet<Point> region, int size, World w) {
		if (t == Terrain.FOREST) {
			return;
		}
		size += RPG.randomize(size) - 1;
		Point source = RPG.pick(new ArrayList<>(region));
		ArrayList<Point> newarea = new ArrayList<>(size);
		newarea.add(source);
		while (size > 0) {
			newarea.add(expand(newarea, region));
			size -= 1;
		}
		int limit = World.scenario.size - 1;
		for (Point p : newarea) {
			if (p.x != 0 && p.y != 0 && p.x != limit && p.y != limit) {
				w.map[p.x][p.y] = t;
			}
		}
	}

	/**
	 * Expands an area.
	 *
	 * @param pool
	 *            Possibilites to start from.
	 * @param region
	 *            Points that are not in this region will be discarded. Ignored
	 *            if <code>null</code>.
	 * @return A new point for the given area.
	 * @throws RestartWorldGeneration
	 *             Through {@link WorldGenerator#retry()}.
	 */
	static public Point expand(List<Point> pool, HashSet<Point> region) {
		int[] steps = new int[] { -1, 0, +1 };
		Point p = new Point(RPG.pick(pool));
		while (pool.contains(p)) {
			p.x += steps[RPG.r(steps.length)];
			p.y += steps[RPG.r(steps.length)];
			if (region != null && !region.contains(p)) {
				p = new Point(RPG.pick(pool));
				WorldGenerator.retry();
			}
		}
		return p;
	}
}