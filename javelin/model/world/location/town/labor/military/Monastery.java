package javelin.model.world.location.town.labor.military;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.view.screen.SquadScreen;

public class Monastery extends Guild {
	static final List<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		for (Monster m : SquadScreen.CANDIDATES) {
			if (!Boolean.FALSE.equals(m.lawful) && m.wisdom >= 8 && m.think(-1)
					&& m.dexterity >= 11) {
				CANDIDATES.add(m);
			}
		}
	}

	public static class BuildMonastery extends BuildAcademy {
		public BuildMonastery() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new Monastery();
		}
	}

	public Monastery() {
		super("Monastery", Kit.MONK, false);
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.MOUNTAINS)) {
			super.generate();
		}
	}

	@Override
	protected List<Monster> getcandidates() {
		return CANDIDATES;
	}

	@Override
	protected Combatant[] generatehires() {
		return new Combatant[] { generatehire(30, "Monk", 6, 10) };
	}
}
