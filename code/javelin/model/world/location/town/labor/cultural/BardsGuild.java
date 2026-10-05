package javelin.model.world.location.town.labor.cultural;

import java.util.HashSet;

import javelin.controller.content.kit.Bard;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.academy.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class BardsGuild extends Guild{
	private static final String DESCRIPTION="Bards guild";

	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildBardsGuild extends BuildAcademy{
		/** Constructor. */
		public BuildBardsGuild(){
			super(Rank.TOWN);
		}

		@Override
		protected Academy generateacademy(){
			return new BardsGuild();
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raise
	 */
	public BardsGuild(){
		super(DESCRIPTION,Bard.INSTANCE);
	}
}
