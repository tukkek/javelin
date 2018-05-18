package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.Realm;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill.SkillUpgrade;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.cultural.MagesGuild;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.model.world.location.unique.SummoningCircle;

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
	public HashSet<Upgrade> fire = new HashSet<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public HashSet<Upgrade> earth = new HashSet<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public HashSet<Upgrade> water = new HashSet<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public HashSet<Upgrade> wind = new HashSet<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public HashSet<Upgrade> good = new HashSet<Upgrade>();
	/** Linked to a {@link Town}'s realm. */
	public HashSet<Upgrade> evil = new HashSet<Upgrade>();
	/** Linked HashSet a {@link Town}'s realm. */
	public HashSet<Upgrade> magic = new HashSet<Upgrade>();

	/** Linked to an {@link MartialAcademy}. */
	public HashSet<Upgrade> combatexpertise = new HashSet<Upgrade>();
	/** Linked to an {@link MartialAcademy}. */
	public HashSet<Upgrade> powerattack = new HashSet<Upgrade>();
	/** Linked to an {@link MartialAcademy}. */
	public HashSet<Upgrade> shots = new HashSet<Upgrade>();

	/** Spell school. */
	public HashSet<Upgrade> schooltotem = new HashSet<Upgrade>();
	/** Spell school. */
	public HashSet<Upgrade> schoolcompulsion = new HashSet<Upgrade>();
	/** Spell school. */
	public HashSet<Upgrade> schoolnecromancy = new HashSet<Upgrade>();
	/** Spell school. */
	public HashSet<Upgrade> schoolconjuration = new HashSet<Upgrade>();
	/** Spell school. */
	public HashSet<Upgrade> schoolevocation = new HashSet<Upgrade>();
	/** Subdomain of conjuration. */
	public HashSet<Upgrade> schoolrestoration = new HashSet<Upgrade>();
	/** Subdomain of necromancy. */
	public HashSet<Upgrade> schoolwounding = new HashSet<Upgrade>();
	/** Spell school; */
	public HashSet<Upgrade> schoolabjuration = new HashSet<Upgrade>();
	/** Spell school; */
	public HashSet<Upgrade> schoolhealwounds = new HashSet<Upgrade>();
	/** Spell school; */
	public HashSet<Upgrade> schooltransmutation = new HashSet<Upgrade>();
	/** Spell school; */
	public HashSet<Upgrade> schooldivination = new HashSet<Upgrade>();
	/**
	 * Used internally for summon spells. For learning {@link Summon}s see
	 * {@link SummoningCircle}.
	 */
	public HashSet<Upgrade> schoolsummoning = new HashSet<Upgrade>();

	/** Internal upgrades. */
	public HashSet<Upgrade> internal = new HashSet<Upgrade>();

	// /**
	// * Gives a starting selection of upgrades to each {@link Town}.
	// */
	// public void distribute() {
	// gather();
	// for (WorldActor p : Town.getall(Town.class)) {
	// Town t = (Town) p;
	// Realm r = t.realm;
	// t.upgrades.add(getclass(r));
	// final List<Upgrade> upgrades =
	// new ArrayList<Upgrade>(getupgrades(r));
	// Collections.shuffle(upgrades);
	// int i = 0;
	// int limit = RPG.r(3, 5);
	// while (t.upgrades.size() < limit && i < upgrades.size()) {
	// t.upgrades.add(upgrades.get(i));
	// i += 1;
	// }
	// }
	// }

	// ClassAdvancement getclass(Realm r) {
	// }

	/**
	 * @param r
	 *            Given a realm...
	 * @return the upgrades that belong to it.
	 */
	public HashSet<Upgrade> getupgrades(Realm r) {
		if (r == javelin.model.Realm.AIR) {
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
		} else if (r == Realm.MAGIC) {
			return magic;
		} else {
			throw new RuntimeException("Uknown town!");
		}
	}

	/** Initializes class, if needed. */
	public void gather() {
		/*
		 * TODO could initialize #getall map here instead of #isempty
		 */
		if (fire.isEmpty()) {
			for (final CrFactor factor : ChallengeCalculator.CR_FACTORS) {
				factor.registerupgrades(this);
			}
		}
	}

	/**
	 * @return all upgrades.
	 */
	public HashMap<String, HashSet<Upgrade>> getall() {
		HashMap<String, HashSet<Upgrade>> all = new HashMap<String, HashSet<Upgrade>>();
		addall(fire, all, "fire");
		addall(earth, all, "earth");
		addall(water, all, "water");
		addall(wind, all, "wind");

		addall(good, all, "good");
		addall(evil, all, "evil");
		addall(magic, all, "magic");

		addall(powerattack, all, "power");
		addall(combatexpertise, all, "expertise");
		addall(shots, all, "shots");

		addall(schoolcompulsion, all, "schoolcompulsion");
		addall(schoolrestoration, all, "schoolrestoration");
		addall(schoolnecromancy, all, "schoolnecromancy");
		addall(schooltotem, all, "schooltotem");
		addall(schoolwounding, all, "schoolwounding");
		addall(schoolconjuration, all, "schoolconjuration");
		addall(schoolabjuration, all, "schoolabjuration");
		addall(schoolevocation, all, "schoolevocation");
		addall(schoolhealwounds, all, "schoolhealwounds");
		addall(schooltransmutation, all, "schooltransmutation");
		addall(schooldivination, all, "schooldivination");
		addall(internal, all, "internal");

		HashSet<Upgrade> classes = new HashSet<Upgrade>();
		ClassLevelUpgrade.init();
		for (ClassLevelUpgrade ca : ClassLevelUpgrade.classes) {
			classes.add(ca);
		}
		addall(classes, all, "classlevels");

		return all;
	}

	static void addall(HashSet<Upgrade> fire2,
			HashMap<String, HashSet<Upgrade>> all, String string) {
		all.put(string, fire2);
	}

	/**
	 * Doesn't count skills, see {@link #countskills()}.
	 *
	 * @return Total number of {@link Upgrade}s available.
	 */
	public int count() {
		int i = 0;
		for (HashSet<Upgrade> l : getall().values()) {
			i += l.size();
		}
		return i - countskills();
	}

	/**
	 * @return Total number of {@link Upgrade}s available.
	 */
	public int countskills() {
		int i = 0;
		for (HashSet<Upgrade> l : getall().values()) {
			for (Upgrade u : l) {
				if (u instanceof SkillUpgrade) {
					i += 1;
				}
			}
		}
		return i;
	}

	/**
	 * @return All {@link FeatUpgrade}.
	 */
	public ArrayList<FeatUpgrade> getfeats() {
		ArrayList<FeatUpgrade> feats = new ArrayList<FeatUpgrade>();
		ArrayList<Upgrade> all = new ArrayList<Upgrade>();
		for (HashSet<Upgrade> realm : getall().values()) {
			all.addAll(realm);
		}
		for (Upgrade u : all) {
			if (u instanceof FeatUpgrade) {
				feats.add((FeatUpgrade) u);
			}
		}
		return feats;
	}

	/**
	 * @return All spells available in the game.
	 */
	public List<Spell> getspells() {
		ArrayList<Spell> spells = new ArrayList<Spell>();
		for (HashSet<Upgrade> category : getall().values()) {
			for (Upgrade u : category) {
				if (u instanceof Spell) {
					spells.add((Spell) u);
				}
			}
		}
		return spells;
	}

	/**
	 * @return All normal upgrades for the given {@link Realm} plus the relevant
	 *         {@link Spell}s which can only be found on {@link MagesGuild}s.
	 */
	public Collection<? extends Upgrade> getfullupgrades(Realm r) {
		HashSet<Upgrade> upgrades = new HashSet<Upgrade>(getupgrades(r));
		for (Spell s : getspells()) {
			if (s.realm.equals(r)) {
				upgrades.add(s);
			}
		}
		return upgrades;
	}

	public HashSet<Upgrade> getalluncategorized() {
		HashSet<Upgrade> all = new HashSet<Upgrade>();
		for (HashSet<Upgrade> upgrades : getall().values()) {
			all.addAll(upgrades);
		}
		return all;
	}
}
