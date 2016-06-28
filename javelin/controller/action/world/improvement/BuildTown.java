package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.model.world.Improvement;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;
import tyrant.mikera.engine.Point;

/**
 * Builds an empty {@link Town}.
 * 
 * @see Work
 * @author alex
 */
public class BuildTown extends Improvement {
	/** Constructor. */
	public BuildTown(String name, double price, Character key) {
		super(name, price, key);
	}

	@Override
	public Location done(int x, int y) {
		Town t = new Town(x, y, World.determinecolor(new Point(x, y)).realm);
		t.description = "your new town";
		t.rename();
		t.lairs.clear();
		t.garrison.clear();
		Research.clear(t);
		return t;
	}
}