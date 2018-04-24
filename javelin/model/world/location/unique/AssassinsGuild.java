package javelin.model.world.location.unique;

import java.util.ArrayList;

import javelin.controller.kit.Assassin;
import javelin.controller.terrain.Terrain;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;

/**
 * An academy dedicated to learning how to Infiltrate.
 *
 * @author alex
 */
public class AssassinsGuild extends Guild {
	static final String DESCRITPION = "Assassins guild";

	public static class BuildAssassinsGuild extends BuildAcademy {
		public BuildAssassinsGuild() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy generateacademy() {
			return new AssassinsGuild();
		}
	}

	/** Constructor. */
	public AssassinsGuild() {
		super(DESCRITPION, Assassin.INSTANCE);
	}

	@Override
	protected void generate() {
		while (x < 0 || Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL)) {
			super.generate();
		}
	}

	public static AssassinsGuild get() {
		ArrayList<Actor> guild = World.getall(AssassinsGuild.class);
		return guild.isEmpty() ? null : (AssassinsGuild) guild.get(0);
	}
}
