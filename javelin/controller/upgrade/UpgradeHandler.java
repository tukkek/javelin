package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.model.world.Town;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.UpgradeOption;

public class UpgradeHandler {
	/**
	 * Not as good as {@link #classes} for surviving the early game but second
	 * to them.
	 */
	public List<Upgrade> defensive = new ArrayList<Upgrade>();
	/**
	 * Upgrades that should be all together at a single town, representing a
	 * trainer.
	 */
	public ArrayList<List<Upgrade>> sets = new ArrayList<List<Upgrade>>();
	/**
	 * Single upgrades that can be anywhere like feats and most spells (both
	 * would make huge sets if were only on one place). Can be used at the end
	 * of the process to even out the number of options in each town.
	 */
	public LinkedList<Upgrade> misc = new LinkedList<Upgrade>();
	LinkedList<Town> townqueue = new LinkedList<Town>();

	public void distribute() {
		for (final CrFactor factor : ChallengeRatingCalculator.CR_FACTORS) {
			factor.listupgrades(this);
		}
		for (List<Upgrade> l : new List[] { defensive, misc, sets }) {
			Collections.shuffle(l);
		}
		seed(defensive);
		while (!sets.isEmpty()) {
			Town target = gettownwithleastupgrades();
			List<Upgrade> most = sets.get(0);
			for (int i = 1; i < sets.size(); i++) {
				List<Upgrade> set = sets.get(i);
				if (set.size() > most.size()) {
					most = set;
				}
			}
			sets.remove(most);
			for (Upgrade u : most) {
				addupgrade(u, target);
			}
		}
		while (!misc.isEmpty()) {
			addupgrade(misc.pop(), gettownwithleastupgrades());
		}
		if (Javelin.DEBUG) {
			debugupgrades();
		}
	}

	private void debugupgrades() {
		int i = 0;
		for (Town t : Town.towns) {
			i += 1;
			System.out.println("Town " + i + " (" + t.upgrades.size() + ")");
			for (Option u : t.upgrades) {
				System.out.println("    " + u.name);
			}
		}
	}

	private Town gettownwithleastupgrades() {
		List<Town> towns = new ArrayList<Town>(Town.towns);
		Collections.shuffle(towns);
		Town least = towns.get(0);
		for (int i = 1; i < towns.size(); i++) {
			Town t = towns.get(i);
			if (t.upgrades.size() < least.upgrades.size()) {
				least = t;
			}
		}
		return least;
	}

	private void seed(List<Upgrade> upgrades) {
		for (Upgrade u : upgrades) {
			addupgrade(u, targetseed());
		}
	}

	public void addupgrade(Upgrade u, Town town) {
		town.upgrades.add(new UpgradeOption(u));
	}

	private Town targetseed() {
		if (townqueue.isEmpty()) {
			townqueue.addAll(Town.towns);
			Collections.shuffle(townqueue);
		}
		return townqueue.pop();
	}

	public ArrayList<Upgrade> addset() {
		ArrayList<Upgrade> list = new ArrayList<Upgrade>();
		sets.add(list);
		return list;
	}
}
