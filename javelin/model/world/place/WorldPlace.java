package javelin.model.world.place;

import java.util.List;

import javelin.JavelinApp;
import javelin.model.world.WorldActor;
import javelin.model.world.WorldMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * A {@link WorldActor} that is actually a place that represent a location to be
 * entered or explored.
 * 
 * @author alex
 */
public abstract class WorldPlace implements WorldActor {

	public int x = -1;

	abstract List<WorldPlace> getall();

	public int y;
	public Thing visual = null;
	protected final String visualname;
	protected final String description;

	public WorldPlace(String visualnamep, String descriptionp) {
		visualname = visualnamep;
		description = descriptionp;
		generate();
		getall().add(this);
	}

	protected void generate() {
		while (x == -1 || WorldMap.isoccupied(x, y, true)) {
			x = RPG.r(0, WorldMap.MAPDIMENSION - 1);
			y = RPG.r(0, WorldMap.MAPDIMENSION - 1);
		}
	}

	@Override
	public int getx() {
		return x;
	}

	@Override
	public int gety() {
		return y;
	}

	@Override
	public void remove() {
		getall().remove(this);
		visual.remove();
	}

	@Override
	public void place() {
		visual = Lib.create(visualname);
		JavelinApp.overviewmap.addThing(visual, x, y);
	}

	public void enter() {
		visual.remove();
		getall().remove(this);
	}

	@Override
	public void move(int tox, int toy) {
		x = tox;
		y = toy;
	}
}