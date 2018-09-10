package javelin.model.world.location.dungeon.temple;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.fight.Fight;
import javelin.controller.fight.TempleEncounter;
import javelin.controller.scenario.Campaign;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.wish.Win;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.item.key.TempleKey;
import javelin.model.item.relic.Relic;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.Images;

/**
 * Temples are the key to winning Javelin's {@link Campaign} mode. Each temple
 * is locked and needs to be unlocked by a {@link TempleKey}, brute
 * {@link Monster#strength} or {@link DisableDevice}. Inside the Temple there
 * will be a {@link Relic}, and once all of those are collected the player can
 * make the {@link Win} wish to finish the game.
 *
 * Each temple is a multi-level {@link TempleDungeon}, where on each floor can
 * be found a special {@link Chest}. The Relic sits on the deepest floor.
 *
 * @see Win
 * @see TempleEncounter
 * @see TempleDungeon#createspecialchest(Point)
 * @author alex
 */
public abstract class Temple extends UniqueLocation {
	/**
	 * TODO there's gotta be a better way to do this
	 */
	public static boolean climbing = false;
	/** TODO same as {@link #climbing} */
	public static boolean leavingfight = false;

	/**
	 * Create the temples during world generation.
	 */
	public static void generatetemples() {
		LinkedList<Integer> els = new LinkedList<>();
		for (int el : new int[] { 3, 5, 8, 10, 13, 15, 18 }) {
			els.add(el);
		}
		Collections.shuffle(els);
		new AirTemple(els.pop()).place();
		new EarthTemple(els.pop()).place();
		new FireTemple(els.pop()).place();
		new WaterTemple(els.pop()).place();
		new EvilTemple(els.pop()).place();
		new GoodTemple(els.pop()).place();
		new MagicTemple(els.pop()).place();
		assert els.isEmpty();
	}

	/**
	 * Reward found on the deepest of the {@link #floors}.
	 *
	 * @see Altar
	 * @see TempleDungeon#deepest
	 */
	public Relic relic;
	/**
	 * A temple needs to be opened by a {@link TempleKey} or other method before
	 * being explored.
	 *
	 * @see TempleKey
	 * @see Scenario#lockedtemples
	 */
	public boolean open = !World.scenario.lockedtemples;
	/**
	 * Each floor has a {@link Chest} with a ruby in it and there is also an
	 * {@link Altar} on the deepest level.
	 */
	public List<TempleDungeon> floors = new ArrayList<>();
	/** Encounter level equivalent for {@link #level}. */
	public int el;
	String fluff;
	/** If not <code>null</code> will override {@link Dungeon#floor}. */
	public String floor = null;
	/** If not <code>null</code> will override {@link Dungeon#wall}. */
	public String wall = null;
	/** If <code>false</code>, draw doors without a wall behind them. */
	public boolean doorbackground = true;
	/** Module level. */
	public int level;
	/** {@link Dungeon} {@link Feature} most likely to be found here. */
	public Class<? extends Feature> feature = null;

	/**
	 * @param r
	 *            Temple's defining characteristic.
	 * @param fluffp
	 *            Text description of temple and surrounding area.
	 */
	public Temple(Realm r, int level, Relic relicp, String fluffp) {
		super("Temple of " + r.getname(), "Temple of " + r.getname(), level,
				level);
		allowedinscenario = false;
		realm = r;
		this.level = level;
		el = level;
		relic = relicp;
		fluff = fluffp;
		link = true;
		generatefloors(level);
	}

	void generatefloors(int level) {
		Tier tier = Tier.get(level);
		int nfloors;
		if (tier == Tier.LOW) {
			nfloors = 2;
		} else if (tier == Tier.PARAGON) {
			nfloors = 4;
		} else {
			nfloors = 3;
		}
		TempleDungeon parent = null;
		for (int i = 0; i < nfloors; i++) {
			boolean deepest = i == nfloors - 1;
			parent = new TempleDungeon(el + i, deepest, parent, this);
			floors.add(parent);
		}
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// no outside garrison
	}

	@Override
	public void place() {
		Realm r = realm;
		super.place();
		realm = r;
	}

	@Override
	public Image getimage() {
		final String name = "locationtemple" + realm.getname().toLowerCase();
		return Images.get(name);
	}

	@Override
	public boolean interact() {
		if (open) {
			floors.get(0).activate(false);
		} else {
			if (!Debug.unlcoktemples && !open()) {
				return true;
			}
			open = true;
			Javelin.message(fluff, true);
		}
		return true;
	}

	boolean open() {
		@SuppressWarnings("deprecation")
		TempleKey key = new TempleKey(realm);
		if (Squad.active.equipment.pop(key) != null) {
			Javelin.message("Temple entrance opened by the "
					+ key.toString().toLowerCase() + "!", true);
			return true;
		}
		Combatant unlock = unlock();
		if (unlock != null) {
			Javelin.message("Temple entrance unlocked by " + unlock + "!",
					true);
			return true;
		}
		Combatant force = force();
		if (force != null) {
			Javelin.message("Temple entrance forced by " + force + "!", true);
			return true;
		}
		Javelin.message("The " + descriptionknown + " is locked.", true);
		return false;
	}

	Combatant force() {
		Combatant best = null;
		for (Combatant c : Squad.active.members) {
			int roll = Monster.getbonus(c.source.strength);
			if (roll < level) {
				continue;
			}
			if (best == null || roll > Monster.getbonus(best.source.strength)) {
				best = c;
			}
		}
		return best;
	}

	Combatant unlock() {
		Combatant best = Squad.active.getbest(Skill.DISABLEDEVICE);
		return best.taketen(Skill.DISABLEDEVICE) >= 10 + level ? best : null;
	}

	@Override
	public Realm getrealmoverlay() {
		return null;
	}

	/**
	 * @return Starts a {@link TempleEncounter}.
	 */
	public Fight encounter(Dungeon d) {
		return new TempleEncounter(this, d);
	}

	/** See {@link Fight#validate(ArrayList)}. */
	public boolean validate(List<Monster> foes) {
		return true;
	}

	/** See {@link Fight#getterrains(Terrain)}; */
	public ArrayList<Terrain> getterrains() {
		ArrayList<Terrain> terrains = new ArrayList<>();
		terrains.add(Terrain.UNDERGROUND);
		terrains.add(terrain);
		return terrains;
	}

	@Override
	protected void generate() {
		if (terrain == null || terrain.equals(Terrain.WATER)) {
			super.generate();
		} else {
			while (x == -1 || !Terrain.get(x, y).equals(terrain)) {
				super.generate();
			}
		}
	}

	/**
	 * See {@link Dungeon#hazard()}.
	 *
	 * @return <code>true</code> if a hazard happens.
	 */
	public boolean hazard(Dungeon templeDungeon) {
		return false;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	public static ArrayList<Temple> gettemples() {
		ArrayList<Temple> temples = new ArrayList<>(7);
		for (Actor a : World.getactors()) {
			if (a instanceof Temple) {
				temples.add((Temple) a);
			}
		}
		return temples;
	}

	@Override
	public String describe() {
		int squad = ChallengeCalculator.calculateel(Squad.active.members);
		String difficulty = Difficulty.describe(level - squad);
		return descriptionknown + " (" + difficulty + ")";
	}
}
