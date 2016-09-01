package javelin.controller.action.world.improvement;

import javelin.controller.Point;
import javelin.controller.action.world.Work;
import javelin.controller.terrain.Forest;
import javelin.controller.terrain.Hill;
import javelin.controller.terrain.Plains;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Improvement;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import tyrant.mikera.engine.RPG;

/**
 * Turns a {@link Forest} tile into a {@link Hill} or {@link Plains}, generating
 * labor in the process.
 * 
 * @see Town#labor
 * @see Work
 * @author alex
 */
public class Deforestate extends Improvement {
	/** Constructor. */
	public Deforestate(String name, double price, Character key, boolean b) {
		super(name, price, key, b);
	}

	@Override
	public Location done(int x, int y) {
		boolean hill = Terrain.checkadjacent(new Point(x, y), Terrain.MOUNTAINS,
				World.seed, 1) > 0;
		if (!hill) {
			hill = RPG.r(1, 8) <= Terrain.checkadjacent(new Point(x, y),
					Terrain.HILL, World.seed, 1);
		}
		World.seed.map[x][y] = hill ? Terrain.HILL : Terrain.PLAIN;
		return null;
	}

	@Override
	public String inform() {
		int labor = Math.round(Math.round(price * Town.DAILYLABOR));
		Squad.active.resources += labor;
		return labor + " resources acquired.\n"
				+ "It can be used to accelerate other Work actions\n"
				+ "or can be converted to labor in a Town.";
	}
}