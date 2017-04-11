package javelin.model.world.location.town.labor.ecological;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.unique.TrainingHall;

public class ArcheryRange extends Guild {
	static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		for (Monster m : TrainingHall.CANDIDATES) {
			if (!m.ranged.isEmpty()) {
				CANDIDATES.add(m);
			}
		}
	}

	public static class BuildArcheryRange extends BuildAcademy {
		public BuildArcheryRange() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new ArcheryRange();
		}
	}

	public ArcheryRange() {
		super("Archery range", Kit.RANGER, false);
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.FOREST)) {
			super.generate();
		}
	}

	@Override
	protected javelin.model.unit.Combatant[] generatehires() {
		return new Combatant[] { generatehire(7, "Tracker", 1, 5),
				generatehire(30, "Ranger", 6, 10) };
	}

	@Override
	protected List<Monster> getcandidates() {
		return CANDIDATES;
	}
}
