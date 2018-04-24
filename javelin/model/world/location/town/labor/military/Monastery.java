package javelin.model.world.location.town.labor.military;

import javelin.controller.kit.Monk;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;

public class Monastery extends Guild {
	public static class BuildMonastery extends BuildAcademy {
		public BuildMonastery() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy() {
			return new Monastery();
		}
	}

	public Monastery() {
		super("Monastery", Monk.INSTANCE);
	}

	@Override
	protected void generate() {
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.MOUNTAINS)) {
			super.generate();
		}
	}
}
