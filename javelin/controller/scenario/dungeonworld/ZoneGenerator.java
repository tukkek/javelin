package javelin.controller.scenario.dungeonworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.WorldGenerator;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.temple.AirTemple;
import javelin.model.world.location.dungeon.temple.EarthTemple;
import javelin.model.world.location.dungeon.temple.EvilTemple;
import javelin.model.world.location.dungeon.temple.FireTemple;
import javelin.model.world.location.dungeon.temple.GoodTemple;
import javelin.model.world.location.dungeon.temple.MagicTemple;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.model.world.location.dungeon.temple.WaterTemple;
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
		int level;

		public Zone(Realm r, int level) {
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
	private Town starting;

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
		placegates(zones.get(0), new HashSet<Zone>(zones.size()));
		shufflegates();
		if (!checksolvable()) {
			throw new RestartWorldGeneration();
		}
		for (Zone z : zones) {
			placefeatures(z);
		}
		return starting;
	}

	void shufflegates() {
		for (Zone z : zones) {
			if (z.gates.size() <= 1) {
				continue;
			}
			ArrayList<Realm> keys = new ArrayList<Realm>(z.gates.size());
			for (Gate g : z.gates) {
				keys.add(g.key);
			}
			Collections.shuffle(keys);
			for (int i = 0; i < z.gates.size(); i++) {
				z.gates.get(i).setkey(keys.get(i));
			}
		}
	}

	boolean checksolvable() {
		HashSet<Zone> visited = new HashSet<Zone>();
		HashSet<Realm> keys = new HashSet<Realm>();
		visited.add(zones.get(0));
		keys.add(zones.get(0).realm);
		while (true) {
			int oldvisited = visited.size();
			for (Zone z : new ArrayList<Zone>(visited)) {
				for (Gate g : z.gates) {
					if (keys.contains(g.key)) {
						Zone to = g.to;
						visited.add(to);
						keys.add(to.realm);
					}
				}
			}
			int newvisited = visited.size();
			if (newvisited == REALMS.size()) {
				return true;
			} else if (newvisited == oldvisited) {
				return false;
			}
		}
	}

	void placefeatures(Zone z) {
		placefeature(createtemple(z.realm, z.level), z);
		Town t = (Town) placefeature(new Town((Point) null, z.realm), z);
		if (starting == null) {
			starting = t;
		}
	}

	Location placefeature(Location l, Zone z) {
		Point p = null;
		while (p == null || world.map[p.x][p.y] == Terrain.WATER
				|| World.get(p.x, p.y) != null || checkclutter(p)) {
			p = RPG.pick(z.arealist);
		}
		l.setlocation(p);
		l.place();
		return l;
	}

	boolean checkclutter(Point p) {
		// TODO check open space7
		return false;
	}

	Temple createtemple(Realm r, int level) {
		if (r == Realm.AIR) {
			return new AirTemple(level);
		}
		if (r == Realm.EARTH) {
			return new EarthTemple(level);
		}
		if (r == Realm.EVIL) {
			return new EvilTemple(level);
		}
		if (r == Realm.FIRE) {
			return new FireTemple(level);
		}
		if (r == Realm.GOOD) {
			return new GoodTemple(level);
		}
		if (r == Realm.MAGIC) {
			return new MagicTemple(level);
		}
		if (r == Realm.WATER) {
			return new WaterTemple(level);
		}
		return null;
	}

	void placegates(Zone from, HashSet<Zone> cache) {
		if (cache.contains(from)) {
			return;
		}
		cache.add(from);
		for (Zone to : from.borders.keySet()) {
			if (!cache.contains(to)) {
				placegate(from, to, from.realm);
				placegates(to, cache);
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
		int limit = World.scenario.size;
		Point gate = null;
		while (gate == null) {
			gate = RPG.pick(new ArrayList<Point>(from.borders.get(to)));
			if (gate.x == 0 || gate.y == 0 || gate.x == limit - 1
					|| gate.y == limit - 1) {
				gate = null;
				continue;
			}
			boolean reacha = false;
			boolean reachb = false;
			for (Point p : Point.getadjacent()) {
				p.x += gate.x;
				p.y += gate.y;
				if (world.map[p.x][p.y] == Terrain.WATER) {
					continue;
				}
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
		Gate g = new Gate(key, to);
		from.gates.add(g);
		g.setlocation(gate);
		g.place();
		clearterrain(gate);
	}

	void clearterrain(Point gate) {
		List<Point> ajacent = Arrays.asList(Point.getadjacent());
		Collections.shuffle(ajacent);
		for (Point p : ajacent) {
			p.x += +gate.x;
			p.y += +gate.y;
			if (!World.validatecoordinate(p.x, p.y)) {
				continue;
			}
			Terrain t = world.map[p.x][p.y];
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
		int nrealms = REALMS.size();
		for (int i = 0; i < nzones; i++) {
			int level = 1 + i * 19 / (nrealms - 1);
			Zone z = new Zone(REALMS.get(i), level);
			System.out.print(level + " ");
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