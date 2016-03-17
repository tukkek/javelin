package javelin.model.world.place;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.model.BattleMap;
import javelin.model.dungeon.Feature;
import javelin.model.dungeon.Fountain;
import javelin.model.dungeon.StairsUp;
import javelin.model.dungeon.Treasure;
import javelin.model.item.Item;
import javelin.model.item.Key;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.world.DungeonScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Tile;

/**
 * A dungeon is an underground area of the world where the combats are harder
 * but have extra treasure laying around.
 * 
 * @author alex
 */
public class Dungeon extends WorldPlace {
	public final static int SIZE = 7 + 1 + 7;
	public final static float WALLRATIO = 1 / 3f;
	public final static int WALKABLEAREA =
			Math.round(SIZE * SIZE * (1 - WALLRATIO));
	/**
	 * Assumes you will explore all the dungeon, being able to rest at the
	 * fountain mid-way for a total of 8 moderate encounters. This ignores the
	 * returning path towards the exit and getting to and from the dungeon
	 * itself, which means squads are supposed to be well equipped before diving
	 * in.
	 */
	public static final double ENCOUNTERRATIO =
			WALKABLEAREA / 8.0 / WALKABLEAREA;

	private static final int MAXTRIES = 1000;
	public final static int[] DELTAS = { -1, 0, 1 };

	public static List<WorldPlace> dungeons = new ArrayList<WorldPlace>();
	public static Dungeon active = null;
	public List<Feature> features = new ArrayList<Feature>();
	public HashSet<Point> walls = new HashSet<Point>();
	public boolean[][] visible;
	public Point herop;

	transient private BattleMap map = null;
	transient public Thing hero;
	transient boolean generated = false;

	public Dungeon() {
		super("stairs down", "a dungeon");
	}

	public static void spawn(float i) {
		if (RPG.random() < i) {
			new Dungeon().place();
		}
	}

	@Override
	public void enter() {
		super.enter();
		activate();
	}

	public void activate() {
		while (map == null) {
			map = new BattleMap(SIZE, SIZE);
			if (features.isEmpty()) {
				/* not loading a game */
				try {
					map();
				} catch (GaveUpException e) {
					map = null;
					features.clear();
					walls.clear();
				}
			}
		}
		regenerate();
		Game.instance().setHero(hero);
		JavelinApp.context = new DungeonScreen(map);
		active = this;
		BattleScreen.active = JavelinApp.context;
		Squad.active.updateavatar();
	}

	public void map() throws GaveUpException {
		final Set<Point> all = new HashSet<Point>();
		Point root = new Point(RPG.r(1, SIZE - 2), RPG.r(1, SIZE - 2));
		build(new StairsUp("stairs up", root), all);
		herop = null;
		while (herop == null || herop.x == 0 && herop.y == 0) {
			herop = new Point(RPG.pick(DELTAS), RPG.pick(DELTAS));
		}
		herop = new Point(root.x + herop.x, root.y + herop.y);
		all.add(herop);
		placewalls(all);
		Point fountain = findspot(all);
		build(new Fountain("fountain", fountain.x, fountain.y), all);
		createchests(all);
		visible = new boolean[SIZE][SIZE];
		for (int x = 0; x < Dungeon.SIZE; x++) {
			for (int y = 0; y < Dungeon.SIZE; y++) {
				visible[x][y] = false;
			}
		}
	}

	public void createchests(final Set<Point> all) {
		int chests = RPG.r(3, 5);
		int pool = RewardCalculator.receivegold(Squad.active.members);
		int traps = RPG.r(0, 7 - chests);
		for (int i = 0; i < traps; i++) {
			// TODO add 1 chest per trap, improve pool
		}
		for (int i = chests; i > 0; i--) {
			int gold = i == 1 ? pool : pool / RPG.r(2, i);
			pool -= gold;
			Point p = findspot(all);
			Treasure t;
			if (i == chests) {
				t = new Treasure("chest", p.x, p.y, 0, new ArrayList<Item>());
				t.key = Key.generate(null);
			} else {
				t = RewardCalculator.createchest(gold, p.x, p.y);
			}
			build(t, all);
		}
	}

	public void placewalls(final Set<Point> all) throws GaveUpException {
		int walltarget =
				Math.round(SIZE * SIZE * WALLRATIO * RPG.r(90, 110) / 100f);
		int tries = 0;
		for (int i = 0; i < walltarget; i++) {
			Point wall = findspot(all);
			int neighborhood = 0;
			wallplacement: for (int x = -1; x <= +1; x++) {
				for (int y = -1; y <= +1; y++) {
					if (x == 0 && y == 0) {
						continue;
					}
					if (walls.contains(new Point(wall.x + x, wall.y + y))) {
						neighborhood += 1;
						if (neighborhood > 1) {
							break wallplacement;
						}
					}
				}
			}
			if (neighborhood < 2) {
				all.add(wall);
				walls.add(wall);
			} else {
				i -= 1;
			}
			tries += 1;
			if (tries > MAXTRIES) {
				throw new GaveUpException();
			}
		}
	}

	void build(Feature feature, Set<Point> all) {
		features.add(feature);
		all.add(new Point(feature.x, feature.y));
	}

	private Point findspot(Set<Point> all) {
		Point p = null;
		while (p == null || all.contains(p)) {
			p = new Point(RPG.r(0, SIZE - 1), RPG.r(0, SIZE - 1));
		}
		return p;
	}

	@Override
			List<WorldPlace> getall() {
		return dungeons;
	}

	public void leave() {
		Dungeon.active = null;
		JavelinApp.context = new WorldScreen(JavelinApp.overviewmap);
		BattleScreen.active = JavelinApp.context;
		hero.remove();
	}

	protected void regenerate() {
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				map.setTile(x, y, Tile.METALFLOOR);
			}
		}
		for (Point wall : walls) {
			map.setTile(wall.x, wall.y, Tile.STONEWALL);
		}
		hero = Squad.active.createThing();
		hero.x = herop.x;
		hero.y = herop.y;
		map.addThing(hero, hero.x, hero.y);
		if (!generated) {
			for (Feature f : features) {
				f.generate(map);
			}
			generated = true;
		}
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		int el = attacker.determineel();
		return Incursion.fight(el, el - 3);
	}

	@Override
	public boolean ignore(Incursion attacker) {
		return false;
	}
}
