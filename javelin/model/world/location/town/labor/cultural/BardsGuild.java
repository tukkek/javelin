package javelin.model.world.location.town.labor.cultural;

import java.util.HashSet;

import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class BardsGuild extends Academy {
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
		super(DESCRIPTION, DESCRIPTION, Kit.BARD.upgrades);
	}

	@Override
	protected void generate() {
		while (x == -1 || !(Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL))) {
			super.generate();
		}
	}
}
