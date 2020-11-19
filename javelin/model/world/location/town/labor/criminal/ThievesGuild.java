package javelin.model.world.location.town.labor.criminal;

import java.util.HashSet;

import javelin.controller.kit.Rogue;
import javelin.model.world.location.Academy;
import javelin.model.world.location.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class ThievesGuild extends Guild{
	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildThievesGuild extends BuildAcademy{
		/** Constructor. */
		public BuildThievesGuild(){
			super(Rank.TOWN);
		}

		@Override
		protected Academy generateacademy(){
			return new ThievesGuild();
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raise
	 */
	public ThievesGuild(){
		super("Thieves guild",Rogue.INSTANCE);
	}
}
