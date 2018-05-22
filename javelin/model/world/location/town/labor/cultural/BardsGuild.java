package javelin.model.world.location.town.labor.cultural;

import java.util.HashSet;

import javelin.controller.kit.Bard;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class BardsGuild extends Guild {
	private static final String DESCRIPTION = "Bards guild";

	public static class BuildBardsGuild extends BuildAcademy {
		public BuildBardsGuild() {
			super(Rank.TOWN);
		}

		@Override
		protected Academy generateacademy() {
			return new BardsGuild();
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raise
	 */
	public BardsGuild() {
		super(DESCRIPTION, Bard.INSTANCE);
	}

	@Override
	protected void generate() {
		while (x == -1 || !(Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL))) {
			super.generate();
		}
	}
}
