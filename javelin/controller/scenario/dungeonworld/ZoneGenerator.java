package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Trait;
import tyrant.mikera.engine.RPG;

public class ZoneGenerator extends FeatureGenerator {
	static final double MINDISTANCE = 5;
	static ArrayList<Realm> REALMS = new ArrayList<Realm>(
			Arrays.asList(Realm.values()));

	static {
		Collections.shuffle(REALMS);
	}

	class Zone {
		HashSet<Point> area = new HashSet<Point>();
		ArrayList<Point> arealist = new ArrayList<Point>();
		HashMap<Zone, HashSet<Point>> borders = new HashMap<Zone, HashSet<Point>>();
		ArrayList<Gate> gates = new ArrayList<Gate>();
		Realm realm;

		public Zone(Realm r) {
			realm = r;
		}

		void add(Point p) {
			if (area.add(p)) {
				arealist.add(p);
			}
		}

		void expand() {
			Point p = DungeonWorldGenerator.expand(arealist, null);
			if (!World.validatecoordinate(p.x, p.y)) {
				return;
			}
			int neighborindex = checkclaimed(p);
			if (neighborindex < 0) {
				add(p);
				claimed += 1;
			} else if (neighborindex != Integer.MAX_VALUE
					&& neighborindex != zones.indexOf(this)) {
				Zone neighbor = zones.get(neighborindex);
				addborder(neighbor, p);
				neighbor.addborder(this, p);
				if (allborders.add(p)) {
					claimed += 1;
				}
			}
		}

		void addborder(Zone neighbor, Point p) {
			area.remove(p);
			arealist.remove(p);
			HashSet<Point> border = borders.get(neighbor);
			if (border == null) {
				border = new HashSet<Point>(1);
				borders.put(neighbor, border);
			}
			border.add(p);
		}

		HashSet<Point> enclose() {
			HashSet<Point> frontier = new HashSet<Point>();
			for (Point territory : area) {
				for (Point p : Point.getadjacent()) {
					p.x += territory.x;
					p.y += territory.y;
					if (World.validatecoordinate(p.x, p.y)
							&& !frontier.contains(p) && !allborders.contains(p)
							&& !area.contains(p)) {
						frontier.add(p);
					}
				}
			}
			return frontier;
		}
	}

	ArrayList<Zone> zones = new ArrayList<Zone>();
	HashSet<Point> allborders = new HashSet<Point>();
	int claimed = 0;
	int worldsize;
	World world;

	@Override
	public void spawn(float chance, boolean generatingworld) {
		// don't: static world
	}

	/**
	 * @return Same {@link Zone} if has been claimed by border.
	 */
	int checkclaimed(Point p) {
		if (allborders.contains(p)) {
			return Integer.MAX_VALUE;
		}
		for (int i = 0; i < zones.size(); i++) {
			Zone z = zones.get(i);
			if (z.area.contains(p)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public Town generate(LinkedList<Realm> realms,
			ArrayList<HashSet<Point>> regions, World w) {
		world = w;
		worldsize = World.scenario.size * World.scenario.size;
		while (zones.isEmpty()) {
			generatezones(7);
		}
		while (claimed < worldsize) {
			RPG.pick(zones).expand();
		}
		createborders();
		createcheckpoints(zones.get(0), new HashSet<Zone>(zones.size()));
		Point p = new Point(World.scenario.size / 2, World.scenario.size / 2);
		Town t = new Town(p, Realm.FIRE);
		t.place();
		return t;
	}

	void createcheckpoints(Zone from, HashSet<Zone> cache) {
		if (cache.contains(from)) {
			return;
		}
		cache.add(from);
		for (Zone to : from.borders.keySet()) {
			if (!cache.contains(to)) {
				placegate(from, to, from.realm);
				createcheckpoints(to, cache);
			} else if (RPG.chancein(2)) {
				placegate(from, to, RPG.pick(REALMS));
			}
		}
		clearmap();
	}

	void clearmap() {
		for (int x = 0; x < World.scenario.size; x++) {
			for (int y = 0; y < World.scenario.size; y++) {
				Point p = new Point(x, y);
				if (checkclaimed(p) < 0) {
					world.map[p.x][p.y] = Terrain.WATER;
				}
			}
		}
	}

	void placegate(Zone from, Zone to, Realm key) {
		Point gate = null;
		int limit = World.scenario.size;
		while (gate == null) {
			gate = RPG.pick(new ArrayList<Point>(from.borders.get(to)));
			if (gate.x == 0 || gate.y == 0 || gate.x == limit - 1
					|| gate.x == limit - 1) {
				continue;
			}
			boolean reacha = false;
			boolean reachb = false;
			for (Point p : Point.getadjacent()) {
				p.x += gate.x;
				p.y += gate.y;
				if (from.area.contains(p)) {
					reacha = true;
				}
				if (to.area.contains(p)) {
					reachb = true;
				}
			}
			if (!reacha || !reachb) {
				gate = null;
				WorldGenerator.retry();
			}
		}
		Gate g = new Gate(key);
		g.setlocation(gate);
		g.place();
		clearterrain(gate);
	}

	void clearterrain(Point gate) {
		List<Point> ajacent = Arrays.asList(Point.getadjacent());
		Collections.shuffle(ajacent);
		for (Point p : ajacent) {
			Terrain t = world.map[p.x + gate.x][p.y + gate.y];
			if (t != Terrain.WATER) {
				world.map[gate.x][gate.y] = t;
				break;
			}
		}
	}

	void createborders() {
		for (Point p : allborders) {
			world.map[p.x][p.y] = Terrain.WATER;
		}
		for (Zone z : zones) {
			HashSet<Point> frontier = z.enclose();
			allborders.addAll(frontier);
			for (Point p : frontier) {
				world.map[p.x][p.y] = Terrain.WATER;
			}
		}
	}

	void generatezones(int nzones) {
		ArrayList<Point> zones = new ArrayList<Point>(nzones);
		while (zones.size() < nzones) {
			Point p = new Point(RPG.r(1, World.scenario.size - 2),
					RPG.r(1, World.scenario.size - 2));
			if (!zones.contains(p)) {
				zones.add(p);
			}
		}
		for (int i = 0; i < nzones; i++) {
			for (int j = i + 1; j < nzones; j++) {
				if (zones.get(i).distance(zones.get(j)) < MINDISTANCE) {
					WorldGenerator.retry();
					return;
				}
			}
		}
		for (int i = 0; i < nzones; i++) {
			Zone z = new Zone(REALMS.get(i));
			z.add(zones.get(i));
			this.zones.add(z);
		}
	}

	Town process(ArrayList<Town> towns) {
		LinkedList<Trait> traits = new LinkedList<Trait>(Deck.TRAITS);
		Collections.shuffle(traits);

		Point p = new Point(World.scenario.size / 2, World.scenario.size / 2);
		Town t = new Town(p, Realm.FIRE);
		t.place();
		return t;
	}
}