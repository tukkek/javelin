package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.controller.upgrade.skill.SkillUpgrade;
import javelin.model.Realm;
import javelin.model.world.WorldActor;
import javelin.model.world.place.guarded.Academy;
import javelin.model.world.place.town.Town;
import tyrant.mikera.engine.RPG;

/**
 * Collects and distributes {@link Upgrade}s from different subsystems.
 * 
 * @author alex
 */
public class UpgradeHandler {
	/** The class can be accessed through here. */
	public final static UpgradeHandler singleton = new UpgradeHandler();

	private UpgradeHandler() {
		// prevents instantiation
	}

	LinkedList<Town> townqueue = new LinkedList<Town>();

	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> fire = new ArrayList<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> earth = new ArrayList<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> water = new ArrayList<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> wind = new ArrayList<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> good = new ArrayList<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> evil = new ArrayList<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public ArrayList<Upgrade> magic = new ArrayList<Upgrade>();

	/** Linked to an {@link Academy}. */
	public ArrayList<Upgrade> expertise = new ArrayList<Upgrade>();
	/** Linked to an {@link Academy}. */
	public ArrayList<Upgrade> power = new ArrayList<Upgrade>();
	/** Linked to an {@link Academy}. */
	public ArrayList<Upgrade> shots = new ArrayList<Upgrade>();

	/**
	 * Gives a starting selection of upgrades to each {@link Town}.
	 */
	public void distribute() {
		gather();
		for (WorldActor p : Town.getall(Town.class)) {
			Town t = (Town) p;
			Realm r = t.realm;
			t.upgrades.add(getclass(r));
			final List<Upgrade> upgrades = getupgrades(r);
			Collections.shuffle(upgrades);
			int i = 0;
			int limit = RPG.r(3, 5);
			while (t.upgrades.size() < limit && i < upgrades.size()) {
				t.upgrades.add(upgrades.get(i));
				i += 1;
			}
		}
	}

	ClassAdvancement getclass(Realm r) {
		if (r == javelin.model.Realm.WIND) {
			return ClassAdvancement.EXPERT;
		} else if (r == Realm.FIRE) {
			return ClassAdvancement.WARRIOR;
		} else if (r == Realm.WATER) {
			return ClassAdvancement.ARISTOCRAT;
		} else if (r == Realm.EARTH) {
			return ClassAdvancement.COMMONER;
		} else if (r == Realm.GOOD) {
			return ClassAdvancement.ARISTOCRAT;
		} else if (r == Realm.EVIL) {
			return ClassAdvancement.COMMONER;
		} else if (r == Realm.MAGICAL) {
			return ClassAdvancement.ARISTOCRAT;
		} else {
			throw new RuntimeException("Uknown town!");
		}
	}

	/**
	 * @param r
	 *            Given a realm...
	 * @return the upgrades that belong to it.
	 */
	public List<Upgrade> getupgrades(Realm r) {
		if (r == javelin.model.Realm.WIND) {
			return wind;
		} else if (r == Realm.FIRE) {
			return fire;
		} else if (r == Realm.WATER) {
			return water;
		} else if (r == Realm.EARTH) {
			return earth;
		} else if (r == Realm.GOOD) {
			return good;
		} else if (r == Realm.EVIL) {
			return evil;
		} else if (r == Realm.MAGICAL) {
			return magic;
		} else {
			throw new RuntimeException("Uknown town!");
		}
	}

	/** Initializes class, if needed. */
	public void gather() {
		if (fire.isEmpty()) {
			for (final CrFactor factor : ChallengeRatingCalculator.CR_FACTORS) {
				factor.listupgrades(this);
			}
		}
	}

	/**
	 * @return all upgrades.
	 */
	public HashMap<String, List<Upgrade>> getall() {
		HashMap<String, List<Upgrade>> all =
				new HashMap<String, List<Upgrade>>();
		addall(fire, all, "fire");
		addall(earth, all, "earth");
		addall(water, all, "water");
		addall(wind, all, "wind");

		addall(good, all, "good");
		addall(evil, all, "evil");
		addall(magic, all, "magic");

		addall(power, all, "power");
		addall(expertise, all, "expertise");
		addall(shots, all, "shots");

		return all;
	}

	private void addall(ArrayList<Upgrade> fire2,
			HashMap<String, List<Upgrade>> all, String string) {
		all.put(string, fire2);
	}

	/**
	 * Doesn't count skills, see {@link #countskills()}.
	 * 
	 * @return Total number of {@link Upgrade}s available.
	 */
	public int count() {
		int i = 0;
		for (List<Upgrade> l : getall().values()) {
			i += l.size();
		}
		return i - countskills();
	}

	/**
	 * @return Total number of {@link Upgrade}s available.
	 */
	public int countskills() {
		int i = 0;
		for (List<Upgrade> l : getall().values()) {
			for (Upgrade u : l) {
				if (u instanceof SkillUpgrade) {
					i += 1;
				}
			}
		}
		return i;
	}

}
