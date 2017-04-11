package javelin.model.world.location.town.labor.cultural;

import java.util.HashSet;

import javelin.controller.kit.Kit;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class BardsGuild extends Academy {
	public static class BuildBardsGuild extends BuildAcademy {
		public BuildBardsGuild() {
			super(Rank.TOWN);
		}

		@Override
		protected Academy getacademy() {
			return new BardsGuild();
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raise
	 */
	public BardsGuild() {
		super("Bards guild", "Bards guild", Kit.BARD.upgrades);
	}
}
