package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.exception.RepeatTurnException;
import javelin.model.Realm;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * An independent overworld feature.
 * 
 * If you're creating a new actor type don't forget to update
 * {@link WorldScreen#getallmapactors()}!
 * 
 * @author alex
 */
public abstract class WorldActor implements Serializable {
	public static HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>> INSTANCES =
			new HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>>();

	public static ArrayList<WorldActor>
			getall(Class<? extends WorldActor> worldplace) {
		ArrayList<WorldActor> all = INSTANCES.get(worldplace);
		if (all == null) {
			all = new ArrayList<WorldActor>();
			INSTANCES.put(worldplace, all);
		}
		return all;
	}

	public WorldActor() {
		registerinstance();
	}

	public int x = -1;
	public int y = -1;
	/**
	 * Which team this actor belongs to or <code>null</code> if neutral.
	 */
	public Realm realm = null;
	/**
	 * If <code>true</code> this actor will be ignored by {@link Incursion}s.
	 */
	public boolean impermeable = false;
	public transient Thing visual;
	String visualname = "dog";

	public void remove() {
		visual.remove();
		deregisterinstance();
	}

	public void place() {
		visual = createvisual();
		WorldScreen.worldmap.addThing(visual, x, y);
	}

	public Thing createvisual() {
		return Lib.create(visualname);
	}

	public void move(int tox, int toy) {
		x = tox;
		y = toy;
	}

	/**
	 * Called when an incursion reaches this actor's location.
	 * 
	 * @see Incursion#ignoreincursion(Incursion)
	 * @see Incursion#fight(int, int)
	 * 
	 * @param incursion
	 *            Attacking incursion.
	 * @return <code>true</code> if this place gets destroyed,
	 *         <code>false</code> if the Incursion is destroyed or
	 *         <code>null</code> if neither.
	 */
	abstract public Boolean destroy(Incursion attacker);

	protected void registerinstance() {
		ArrayList<WorldActor> list = INSTANCES.get(getClass());
		if (list == null) {
			list = new ArrayList<WorldActor>(1);
			INSTANCES.put(getClass(), list);
		}
		list.add(this);
	}

	protected void deregisterinstance() {
		List<WorldActor> list = INSTANCES.get(getClass());
		if (list != null) {
			list.remove(this);
		}
	}

	public void displace() {
		int deltax = 0, deltay = 0;
		int[] nudges = new int[] { -1, 0, +1 };
		while (deltax == 0 && deltay == 0) {
			deltax = RPG.pick(nudges);
			deltay = RPG.pick(nudges);
		}
		int tox = x + deltax;
		int toy = y + deltay;
		ArrayList<WorldActor> actors = WorldScreen.getactors();
		actors.remove(this);
		if (tox >= 0 && toy >= 0 && tox < World.MAPDIMENSION
				&& toy < World.MAPDIMENSION
				&& WorldScreen.getactor(tox, toy, actors) == null) {
			move(tox, toy);
		} else {
			displace();
		}
	}

	/** Called on each instance once per day. */
	public void turn(long time, WorldScreen world) {
		// nothing by default
	}

	/**
	 * Called when a {@link Squad} enters the same world square as this actor.
	 * 
	 * @throws RepeatTurnException
	 */
	public boolean interact() {
		return false;
	}
}
