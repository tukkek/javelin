package javelin.model.world.location.town.labor.criminal;

import java.util.HashSet;

import javelin.controller.kit.Rogue;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class ThievesGuild extends Guild{
	public static class BuildThievesGuild extends BuildAcademy{
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

	@Override
	protected void generate(){
		while(x==-1||Terrain.get(x,y).equals(Terrain.DESERT))
			super.generate();
	}
}
