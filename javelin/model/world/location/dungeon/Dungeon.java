package javelin.model.world.location.dungeon;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.item.ItemSelection;
import javelin.model.item.Key;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.temple.TempleDungeon;
import javelin.model.world.location.dungeon.temple.features.Brazier;
import javelin.model.world.location.dungeon.temple.features.FruitTree;
import javelin.model.world.location.dungeon.temple.features.Portal;
import javelin.model.world.location.dungeon.temple.features.Spirit;
import javelin.model.world.location.dungeon.temple.features.StairsDown;
import javelin.view.Images;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * A dungeon is an underground area of the world where the combats are harder
 * but have extra treasure laying around.
 *
 * The in-game logic for dungeons is that they are a hideout of bandits or
 * similar, which is why they are sacrificeable by {@link Incursion}s and are
 * removed from the game after a {@link Squad} leaves one (in this case it's
 * assumed the bandits packed their stuff and left).
 *
 * @author alex
 */
public class Dungeon extends Location {
	static protected class DungeonTier {
		int el;
		String name;
		int minrooms;
		int maxrooms;

		public DungeonTier(String name, int level, int minrooms, int maxrooms) {
			this.name = name;
			this.minrooms = minrooms;
			this.maxrooms = maxrooms;
			el = level;
		}
	}

	static final int MAXTRIES = 1000;
	static final int[] DELTAS = { -1, 0, 1 };
	static final DungeonTier[] TIERS = new DungeonTier[] {
			new DungeonTier("Cave", 5, 5, 7),
			new DungeonTier("Dungeon", 10, 5, 10),
			new DungeonTier("Ruins", 15, 10, 15),
			new DungeonTier("Keep", 20, 10, 20), };

	/** Current {@link Dungeon} or <code>null</code> if not in one. */
	public static Dungeon active = null;

	/** All of this dungeon's {@link Feature}s. */
	public List<Feature> features = new ArrayList<Feature>();
	/**
	 * Explored squares in this dungeon.
	 *
	 * TODO why is there this and also {@link #discovered}?
	 */
	public boolean[][] visible;
	/**
	 * Current {@link Squad} location.
	 *
	 * TODO is this needed?
	 */
	public Point herolocation;
	/** File to use under 'avatar' folder. */
	public String floor = "dungeonfloor";
	/** File to use under 'avatar' folder. */
	public String wall = "dungeonwall";
	/** Tiles already revealed. */
	public HashSet<Point> discovered = new HashSet<Point>();
	public char[][] map;
	public int size;
	public float encounterratio;
	public int stepsperencounter;
	public int el = -1;

	transient boolean generated = false;

	/** Constructor. */
	public Dungeon() {
		super("A dungeon");
		link = false;
		discard = false;
		impermeable = true;
		determineel();
	}

	protected void determineel() {
		List<Dungeon> dungeons = getdungeons();
		if (dungeons.size() >= 20) {
			el = RPG.r(1, 20);
			return;
		}
		generating: while (el == -1) {
			el = RPG.r(1, 20);
			for (Dungeon d : dungeons) {
				if (d.el == el) {
					continue generating;
				}
			}
		}
	}

	@Override
	public boolean interact() {
		super.interact();
		activate(false);
		return true;
	}

	/** Create or recreate dungeon. */
	public void activate(boolean loading) {
		while (features.isEmpty()) {
			/* not loading a game */
			map();
		}
		regenerate(loading);
		JavelinApp.context = new DungeonScreen(this);
		active = this;
		BattleScreen.active = JavelinApp.context;
		Squad.active.updateavatar();
		BattleScreen.active.mappanel.center(herolocation.x, herolocation.y,
				true);
	}

	/**
	 * This function generates the dungeon map using {@link DungeonGenerator}
	 * and then {@link #placefeatures()}. One notable thing that happens here is
	 * the determination of how many {@link RandomDungeonEncounter}s should take
	 * for the player to explore the whole level.
	 * 
	 * Currently, the calculation is done by setting a goal of one fight per
	 * room on average (so naturally, larger {@link DungeonTier}s will have more
	 * fights than smaller ones). The formula takes into account
	 * {@link DungeonScreen#VIEWRADIUS} instead of counting each step as a
	 * single tile.
	 * 
	 * Since a Squad of the dungeon's intended level cannot hope to clear a
	 * dungeon if it's large (in average they can only take 4-5 encounters of
	 * the same EL), this is then offset by placing enough fountains that would
	 * theoretically allow them to do the one dungeon in one go.
	 * 
	 * This is currently not counting backtracking out of the dungeon or finding
	 * your way back to town safely, so this naturally makes the dungeon more
	 * challenging (hopefully being offset by the rewards inside).
	 */
	void map() {
		DungeonTier tier = gettier();
		DungeonGenerator generator = DungeonGenerator.generate(tier.minrooms,
				tier.maxrooms);
		map = generator.grid;
		size = map.length;
		int vision = DungeonScreen.VIEWRADIUS * 2 + 1;
		int rooms = generator.map.rooms.size();
		int stepsperroom = countfloor() / rooms;
		stepsperencounter = Math.round(stepsperroom / vision);
		encounterratio = 1f / stepsperencounter;
		placefeatures(getfountains(rooms));
		visible = new boolean[size][size];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				visible[x][y] = false;
			}
		}
	}

	public int getfountains(int rooms) {
		return (rooms - 4) / 4;
	}

	int countfloor() {
		int floortiles = 0;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				char tile = map[x][y];
				if (tile == Template.WALL || tile == Template.DECORATION) {
					floortiles += 1;
				}
			}
		}
		return floortiles;
	}

	/**
	 * Places {@link Fountain}, {@link Chest}s and {@link Trap}s then those from
	 * {@link #getextrafeatures(Set, Set)}.
	 * 
	 * @param fountains
	 *
	 * @param free
	 * @param used
	 */
	protected void placefeatures(int fountains) {
		herolocation = findspot();
		features.add(new StairsUp("stairs up", herolocation));
		for (int x = herolocation.x - 1; x <= herolocation.x + 1; x++) {
			for (int y = herolocation.y - 1; y <= herolocation.y + 1; y++) {
				map[x][y] = Template.FLOOR;
			}
		}
		createchests();
		for (Feature f : features) {
			if (f instanceof Trap && RPG.chancein(4)) {
				fountains += 1;
			}
		}
		for (int i = 0; i < fountains; i++) {
			Point fountain = findspot();
			features.add(new Fountain(fountain.x, fountain.y));
		}
		for (Feature f : getextrafeatures()) {
			features.add(f);
		}
	}

	/**
	 * @return <code>true</code> if given point is between 0 and {@link #SIZE}.
	 */
	public boolean valid(int coordinate) {
		return 0 <= coordinate && coordinate <= size;
	}

	void createchests() {
		DungeonTier tier = gettier();
		int chests = RPG.r(tier.minrooms, tier.maxrooms)
				+ RPG.randomize(tier.minrooms);
		int pool = getgoldpool();
		int traps = chests + RPG.randomize(tier.minrooms);
		for (int i = 0; i < traps; i++) {
			int cr = Math.round(getcr()) + EncounterGenerator.getdifficulty();
			Trap t = new Trap(cr, findspot());
			features.add(t);
			pool += RewardCalculator.getgold(t.cr);
		}
		for (int i = chests; i > 0; i--) {
			int gold = i == 1 ? pool : pool / RPG.r(2, i);
			pool -= gold;
			Point p = findspot();
			Feature c;
			if (i == chests && World.scenario.allowkeys) {
				c = createspecialchest(p);
			} else {
				c = Chest.create(gold, p.x, p.y);
			}
			features.add(c);
		}
	}

	public int getgoldpool() {
		return RewardCalculator.getgold(getcr());
	}

	float getcr() {
		return ChallengeCalculator.eltocr(el);
	}

	/**
	 * @param p
	 *            Chest's location.
	 * @return Most special chest here.
	 */
	protected Feature createspecialchest(Point p) {
		Chest t = new Chest(p.x, p.y, 0, new ItemSelection());
		t.key = Key.generate();
		return t;
	}

	/**
	 * @param freep
	 *            Pick a location from here...
	 * @param used
	 *            as long as it's not being used...
	 * @return and returns it.
	 */
	public Point findspot() {
		Point p = null;
		while (p == null || isoccupied(p)) {
			p = new Point(RPG.r(0, size - 1), RPG.r(0, size - 1));
		}
		return p;
	}

	public boolean isoccupied(Point p) {
		if (map[p.x][p.y] == Template.WALL) {
			return true;
		}
		for (Feature f : features) {
			if (f.x == p.x && f.y == p.y) {
				return true;
			}
		}
		return false;
	}

	/** Exit and destroy this dungeon. */
	public void leave() {
		DungeonScreen.dontenter = true;
		JavelinApp.context = new WorldScreen(true);
		BattleScreen.active = JavelinApp.context;
		Squad.active.place();
		Dungeon.active = null;
	}

	void regenerate(boolean loading) {
		setlocation(loading);
		if (!generated) {
			for (Feature f : features) {
				f.generate();
			}
			generated = true;
		}
	}

	/**
	 * Responsible for deciding where the player should be.
	 *
	 * @param loading
	 *            <code>true</code> if activating this {@link Dungeon} when
	 *            starting the application (loading up a save game with an
	 *            active dungeon).
	 */
	protected void setlocation(boolean loading) {
	}

	@Override
	public Integer getel(int attackel) {
		return attackel - 3;
	}

	/**
	 * Called when reaching {@link StairsUp}
	 */
	public void goup() {
		Dungeon.active.leave();
	}

	/**
	 * Called when reaching {@link StairsDown}.
	 */
	public void godown() {
		// typical dungeons have 1 level only
	}

	/** See {@link WorldScreen#encounter()}. */
	public Fight encounter() {
		return new RandomDungeonEncounter(this);
	}

	/**
	 * Akin to terrain {@link Hazard}s.
	 *
	 * @return <code>true</code> if a hazard happens.
	 */
	public boolean hazard() {
		// no hazards in normal dungeons
		return false;
	}

	/**
	 * Similar to {@link #placefeatures(Set, Set)} but usually reserved to
	 * placing {@link TempleDungeon} {@link Feature}s.
	 *
	 * Called after placing all basic features.
	 *
	 * @param used
	 *            Don't forget to update this as you generate new features!
	 * @return Extra features to be placed using {@link #build(Feature, Set)}.
	 */
	protected List<Feature> getextrafeatures() {
		ArrayList<Feature> extra = new ArrayList<Feature>();
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot();
			extra.add(new Brazier(spot.x, spot.y));
		}
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot();
			extra.add(new FruitTree(spot.x, spot.y));
		}
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot();
			extra.add(new Portal(spot.x, spot.y));
		}
		if (RPG.r(1, 10) == 1) {
			Point spot = findspot();
			extra.add(new Spirit(spot.x, spot.y));
		}
		return extra;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	public Feature getfeature(int x, int y) {
		for (Feature f : features) {
			if (f.x == x && f.y == y) {
				return f;
			}
		}
		return null;
	}

	public void setvisible(int x, int y) {
		visible[x][y] = true;
		BattleScreen.active.mappanel.tiles[x][y].discovered = true;
	}

	static public List<Dungeon> getdungeons() {
		ArrayList<Actor> actors = World.getall(Dungeon.class);
		ArrayList<Dungeon> dungeons = new ArrayList<Dungeon>(actors.size());
		for (Actor a : actors) {
			dungeons.add((Dungeon) a);
		}
		return dungeons;
	}

	protected DungeonTier gettier() {
		for (DungeonTier t : TIERS) {
			if (el <= t.el) {
				return t;
			}
		}
		return TIERS[3];
	}

	@Override
	public String describe() {
		int squadel = ChallengeCalculator.calculateel(Squad.active.members);
		String difficulty = ChallengeCalculator
				.describedifficulty(el - squadel);
		return gettier().name + " (" + difficulty + ")";
	}

	@Override
	public Image getimage() {
		return Images.getImage("location" + gettier().name.toLowerCase());
	}
}
