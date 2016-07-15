package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.controller.terrain.Forest;
import javelin.controller.terrain.Hill;
import javelin.controller.terrain.Plains;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Improvement;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import tyrant.mikera.engine.Point;
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
	/**
	 * Constructor.
	 */
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
		Town t = getclosestalliedtown();
		if (t == null) {
			return null;
		}
		int labor = Math.round(Math.round(2 * price / Town.LABORPERIOD));
		t.labor += labor;
		return labor + " labor is transferred to " + t.toString() + ".";
	}

	Town getclosestalliedtown() {
		Town closest = null;
		for (WorldActor a : Town.getall(Town.class)) {
			Town t = (Town) a;
			if (t.ishostile()) {
				continue;
			}
			if (closest == null
					|| t.distance(Squad.active.x, Squad.active.y) < closest
							.distance(Squad.active.x, Squad.active.y)) {
				closest = t;
			}
		}
		return closest;
	}
}