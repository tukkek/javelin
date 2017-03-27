package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Realm;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.Images;

public class RealmAcademy extends Academy {
	public static class BuildAcademy extends Build {
		Realm r;

		public BuildAcademy() {
			super("Build academy", 10);
		}

		@Override
		protected void define() {
			super.define();
			r = town.originalrealm;
			cost = Math.min(cost, getupgrades(r).size());
		}

		@Override
		public Location getgoal() {
			return new RealmAcademy(r);
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d)
					&& d.getlocation(RealmAcademy.class) == null;
		}
	}

	class UpgradeRealmAcademy extends BuildingUpgrade {
		public UpgradeRealmAcademy(int cost, RealmAcademy previous) {
			super("", cost, +cost, previous);
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
			previous.level += cost;
			refill();
		}
	}

	Realm upgradetype;

	public RealmAcademy(Realm r) {
		super("An academy", "An academy", 0, 0, new HashSet<Upgrade>(), null,
				null);
		upgradetype = r;
		level = Math.min(10, getupgrades(upgradetype).size());
		pillage = false;
		refill();
	}

	void refill() {
		ArrayList<Upgrade> upgrades = new ArrayList<Upgrade>(
				getupgrades(upgradetype));
		Collections.shuffle(upgrades);
		for (Upgrade u : upgrades) {
			if (this.upgrades.size() >= level) {
				break;
			}
			this.upgrades.add(u);
		}
	}

	static HashSet<Upgrade> getupgrades(Realm r) {
		return UpgradeHandler.singleton.getupgrades(r);
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> getupgrades = super.getupgrades(d);
		if (upgrades.size() <= d.town.getrank() * 5) {
			getupgrades.add(new UpgradeRealmAcademy(
					getupgrades(upgradetype).size() - upgrades.size(), this));
		}
		return getupgrades;
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		targetel = 1;
	}

	@Override
	public void raiselevel(int bonus) {
		// dont
	}

	@Override
	public Image getimage() {
		return Images.getImage("locationacademy");
	}
}
