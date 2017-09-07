package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;

public class MeadHall extends Guild {
	static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		for (float cr : new float[] { 1f, 1.25f, 1.5f, 1.75f, 2f }) {
			candidates.addAll(Javelin.MONSTERSBYCR.get(cr));
		}
		searching: for (Monster m : candidates) {
			if (!m.think(-1) || Boolean.TRUE.equals(m.lawful)) {
				continue searching;
			}
			int power = m.strength;
			for (int ability : new int[] { m.dexterity, m.intelligence,
					m.wisdom, m.charisma }) {
				if (ability > power) {
					continue searching;
				}
			}
			CANDIDATES.add(m);
		}
	}

	public static class BuildMeadHall extends BuildAcademy {
		public BuildMeadHall() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new MeadHall();
		}
	}

	public MeadHall() {
		super("Mead hall", Kit.BARBARIAN, true);
	}

	@Override
	protected void generate() {
		while (x == -1 || !(Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL))) {
			super.generate();
		}
	}

	@Override
	protected Combatant[] generatehires() {
		return new Combatant[] { generatehire(7, "Whelp", 1, 5),
				generatehire(30, "Barbarian", 6, 10),
				generatehire(100, "Chieftain", 11, 15) };
	}

	@Override
	protected List<Monster> getcandidates() {
		return CANDIDATES;
	}
}
