package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.spell.Summon;
import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.military.Academy;
import tyrant.mikera.engine.RPG;

public class Henge extends Academy {
	static final String DESCRIPTIION = "Henge";

	public static class BuildHenge extends BuildAcademy {
		public BuildHenge() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new Henge(5);
		}
	}

	public class UpgradeHenge extends BuildingUpgrade {
		ArrayList<Monster> newsummons;
		private Henge h;

		public UpgradeHenge(Henge h) {
			super("", 0, 0, h, null);
			this.h = h;
			name = "Upgrade henge";
			newsummons = h.fill(h.level + 5);
			upgradelevel = cost = newsummons.size();
			minimumrank = Rank.get(h.level + cost);
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public boolean validate(District d) {
			return cost != 0 && h.level + cost <= 20 && super.validate(d);
		}

		@Override
		public void done() {
			super.done();
			h.level += cost;
			addsummons(newsummons);
		}
	}

	public Henge(int level) {
		super(DESCRIPTIION, DESCRIPTIION, level - 1, level + 1,
				Kit.DRUID.upgrades, null, null);
		for (Upgrade u : new ArrayList<Upgrade>(upgrades)) {
			if (u instanceof Summon) {
				upgrades.remove(u);
			}
		}
		this.level = level;
		ArrayList<Monster> summons = fill(level);
		if (Javelin.DEBUG && summons.isEmpty()) {
			throw new RuntimeException("Empty summons! #henge");
		}
		addsummons(summons);
	}

	public Henge() {
		this(RPG.r(10, 20));
	}

	void addsummons(ArrayList<Monster> fill) {
		for (Monster m : fill) {
			upgrades.add(new Summon(m.name));
		}
	}

	ArrayList<Monster> fill(int newlevel) {
		ArrayList<Monster> animals = new ArrayList<Monster>();
		for (Float tier : Javelin.MONSTERSBYCR.keySet()) {
			if (tier > level) {
				break;
			}
			for (Monster m : Javelin.MONSTERSBYCR.get(tier)) {
				if (m.type.equals("animal") && !contains(m)) {
					animals.add(m);
				}
			}
		}
		if (animals.isEmpty()) {
			return animals;
		}
		Collections.shuffle(animals);
		while (animals.size() + upgrades.size() > newlevel) {
			animals.remove(0);
		}
		return animals;
	}

	boolean contains(Monster m) {
		for (Upgrade u : upgrades) {
			Summon s = u instanceof Summon ? (Summon) u : null;
			if (s != null && s.monstername.equals(m.name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.FOREST)
				&& !Terrain.get(x, y).equals(Terrain.HILL)) {
			super.generate();
		}
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> upgrades = super.getupgrades(d);
		upgrades.add(new UpgradeHenge(this));
		return upgrades;
	}
}
