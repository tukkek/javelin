package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.Realm;
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.UpgradeOption;
import tyrant.mikera.engine.RPG;

/**
 * Collects and distributes {@link Upgrade}s from different subsystems.
 * 
 * @author alex
 */
public class UpgradeHandler {
	LinkedList<Town> townqueue = new LinkedList<Town>();

	public ArrayList<Upgrade> fire = new ArrayList<Upgrade>();
	public ArrayList<Upgrade> earth = new ArrayList<Upgrade>();
	public ArrayList<Upgrade> water = new ArrayList<Upgrade>();
	public ArrayList<Upgrade> wind = new ArrayList<Upgrade>();
	public ArrayList<Upgrade> good = new ArrayList<Upgrade>();
	public ArrayList<Upgrade> evil = new ArrayList<Upgrade>();
	public ArrayList<Upgrade> magic = new ArrayList<Upgrade>();

	public void distribute() {
		gather();
		for (Town t : Town.towns) {
			Realm r = t.realm;
			t.upgrades.add(new UpgradeOption(getclass(r)));
			final List<Upgrade> upgrades = getupgrades(r);
			Collections.shuffle(upgrades);
			int i = 0;
			while (t.upgrades.size() < RPG.r(3, 5) && i < upgrades.size()) {
				t.upgrades.add(new UpgradeOption(upgrades.get(i)));
				i += 1;
			}
		}
	}

	private ClassAdvancement getclass(Realm r) {
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
		} else if (r == Realm.MAGIC) {
			return ClassAdvancement.ARISTOCRAT;
		} else {
			throw new RuntimeException("Uknown town!");
		}
	}

	List<Upgrade> getupgrades(Realm r) {
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
		} else if (r == Realm.MAGIC) {
			return magic;
		} else {
			throw new RuntimeException("Uknown town!");
		}
	}

	public void gather() {
		for (final CrFactor factor : ChallengeRatingCalculator.CR_FACTORS) {
			factor.listupgrades(this);
		}
	}

	public void addupgrade(Upgrade u, Town town) {
		town.upgrades.add(new UpgradeOption(u));
	}

	public int count() {
		return fire.size() + earth.size() + water.size() + wind.size()
				+ good.size() + evil.size() + magic.size();
	}
}
