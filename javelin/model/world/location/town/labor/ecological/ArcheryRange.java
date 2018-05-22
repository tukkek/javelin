package javelin.model.world.location.town.labor.ecological;

import javelin.controller.kit.Ranger;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;

public class ArcheryRange extends Guild {
	public static class BuildArcheryRange extends BuildAcademy {
		public BuildArcheryRange() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy() {
			return new ArcheryRange();
		}
	}

	public ArcheryRange() {
		super("Archery range", Ranger.INSTANCE);
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.FOREST)) {
			super.generate();
		}
	}
}
