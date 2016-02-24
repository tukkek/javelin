package javelin.controller.upgrade;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.CrFactor;
import javelin.model.world.Town;
import javelin.view.screen.town.option.UpgradeOption;

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
			final List<Upgrade> upgrades;
			if (t.color == null) {
				upgrades = wind;
			} else if (t.color == Color.red) {
				upgrades = fire;
			} else if (t.color == Color.blue) {
				upgrades = water;
			} else if (t.color == Color.green) {
				upgrades = earth;
			} else if (t.color == Color.white) {
				upgrades = good;
			} else if (t.color == Color.black) {
				upgrades = evil;
			} else if (t.color == Color.magenta) {
				upgrades = magic;
			} else {
				throw new RuntimeException("Uknown town!");
			}
			for (Upgrade u : upgrades) {
				t.upgrades.add(new UpgradeOption(u));
			}
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
