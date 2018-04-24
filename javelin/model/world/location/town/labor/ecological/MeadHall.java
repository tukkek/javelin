package javelin.model.world.location.town.labor.ecological;

import javelin.controller.kit.Barbarian;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;

public class MeadHall extends Guild {
	public static class BuildMeadHall extends BuildAcademy {
		public BuildMeadHall() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy() {
			return new MeadHall();
		}
	}

	public MeadHall() {
		super("Mead hall", Barbarian.INSTANCE);
	}

	@Override
	protected void generate() {
		while (x == -1 || !(Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL))) {
			super.generate();
		}
	}
}
