package javelin.model.world.location.town.labor.military;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.Realm;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;

public class RealmAcademy extends Academy {
	public static class BuildRealmAcademy extends BuildAcademy {
		public BuildRealmAcademy() {
			super(Rank.HAMLET);
		}

		@Override
		protected RealmAcademy generateacademy() {
			return new RealmAcademy(town.originalrealm);
		}

		@Override
		protected void define() {
			super.define();
		}
	}

	class UpgradeRealmAcademy extends BuildingUpgrade {
		public UpgradeRealmAcademy(int cost, RealmAcademy previous) {
			super("", cost, +cost, previous, Rank.HAMLET);
			name = "Upgrade academy";
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d) && cost > 0;
		}

		@Override
		public void done() {
			super.done();
			((RealmAcademy) previous).level += cost;
			refill();
		}
	}

	Realm upgradetype;

	/**
	 * Builds a basic, upgradeable academy.
	 *
	 * @param r
	 *            Type of upgrade to offer. If you choose to set this as
	 *            <code>null</code>, you need to manually call
	 *            {@link #setrealm(Realm)} later on.
	 *
	 * @see BuildAcademy
	 * @see UpgradeHandler
	 */
	public RealmAcademy(Realm r) {
		super("", "Academy", 0, 0, new HashSet<Upgrade>(), null, null);
		upgradetype = World.scenario.randomrealms ? Realm.random() : r;
		descriptionknown = r.prefixate() + " academy";
		level = minlevel = maxlevel = Math.min(10, getupgrades(r).size());
		if (minlevel > 1) {
			minlevel -= 1;
		}
		maxlevel += 1;
		refill();
	}

	protected void refill() {
		ArrayList<Upgrade> upgrades = new ArrayList<Upgrade>(
				getupgrades(upgradetype));
		if (this.upgrades.isEmpty()) {
			for (Upgrade u : upgrades) {
				if (u instanceof ClassLevelUpgrade) {
					this.upgrades.add(u);
					break;
				}
			}
		}
		Collections.shuffle(upgrades);
		for (Upgrade u : upgrades) {
			if (this.upgrades.size() >= level) {
				break;
			}
			this.upgrades.add(u);
		}
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> getupgrades = super.getupgrades(d);
		if (upgrades.size() <= d.town.getrank().maxpopulation) {
			getupgrades.add(new UpgradeRealmAcademy(
					getupgrades(upgradetype).size() - upgrades.size(), this));
		}
		return getupgrades;
	}
}