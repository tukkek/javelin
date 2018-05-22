package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.controller.kit.Druid;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;

public class Henge extends Guild {
	static final String DESCRIPTIION = "Henge";

	public static class BuildHenge extends BuildAcademy {
		public BuildHenge() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy() {
			return new Henge();
		}
	}

	public Henge() {
		super(DESCRIPTIION, Druid.INSTANCE);
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
}
