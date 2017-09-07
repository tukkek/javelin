package javelin.model.world.location.town.labor.criminal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.screen.SquadScreen;

/**
 * Allows a player to learn one upgrade set.
 *
 * @author alex
 */
public class ThievesGuild extends Guild {
	static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		for (Monster m : SquadScreen.CANDIDATES) {
			if (!Boolean.TRUE.equals(m.good) && !Boolean.TRUE.equals(m.lawful)
					&& m.think(-1)) {
				CANDIDATES.add(m);
			}
		}
	}

	public static class BuildThievesGuild extends BuildAcademy {
		public BuildThievesGuild() {
			super(Rank.TOWN);
		}

		@Override
		protected Academy getacademy() {
			return new ThievesGuild();
		}
	}

	/**
	 * See {@link Academy#Academy(String, String, int, int, HashSet)}.
	 *
	 * @param raise
	 */
	public ThievesGuild() {
		super("Thieves guild", Kit.ROGUE, true);
	}

	@Override
	protected void generate() {
		while (x == -1 || Terrain.get(x, y).equals(Terrain.DESERT)) {
			super.generate();
		}
	}

	@Override
	protected List<Monster> getcandidates() {
		return CANDIDATES;
	}

	@Override
	protected Combatant[] generatehires() {
		return new Combatant[] { generatehire(7, "Footpad", 1, 5),
				generatehire(30, "Cutpurse", 6, 10),
				generatehire(100, "Rogue", 11, 15) };
	}
}
