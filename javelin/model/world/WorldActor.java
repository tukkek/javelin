package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.exception.RepeatTurn;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * An independent overworld feature.
 * 
 * If you're creating a new actor type don't forget to update
 * {@link WorldActor#getallmapactors()}!
 * 
 * @author alex
 */
public abstract class WorldActor implements Serializable {
	/** Contains all actor instances still in the game. */
	public static HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>> INSTANCES =
			new HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>>();
	static final int[] NUDGES = new int[] { -1, 0, +1 };

	/** Constructor. */
	public WorldActor() {
		registerinstance();
	}

	/** x coordinate. */
	public int x = -1;
	/** y coordinate. */
	public int y = -1;
	/**
	 * Which team this actor belongs to or <code>null</code> if neutral.
	 */
	public Realm realm = null;
	/**
	 * If <code>true</code> this actor will be ignored by {@link Incursion}s.
	 */
	public boolean impermeable = false;
	/** TODO shouldn't need {@link Thing}s on 2.0+ */
	public transient Thing visual;
	String visualname = "dog";

	/** Definitely removes this actor from the game. */
	public void remove() {
		visual.remove();
		deregisterinstance();
	}

	/** Adds this actor to the game. Should only be used once in theory. */
	public void place() {
		visual = createvisual();
		WorldScreen.worldmap.addThing(visual, x, y);
	}

	/** TODO remove on 2.0+ */
	public Thing createvisual() {
		return Lib.create(visualname);
	}

	/** Move actor to the given coordinates. */
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

	/** Called during construction to setup {@link #INSTANCES}. */
	protected void registerinstance() {
		ArrayList<WorldActor> list = INSTANCES.get(getClass());
		if (list == null) {
			list = new ArrayList<WorldActor>(1);
			INSTANCES.put(getClass(), list);
		}
		list.add(this);
	}

	/** Removes this instance from {@link #INSTANCES}. */
	protected void deregisterinstance() {
		List<WorldActor> list = INSTANCES.get(getClass());
		if (list != null) {
			list.remove(this);
		}
	}

	/**
	 * Moves actor to nearby square until a free square is found.
	 */
	public void displace() {
		int deltax = 0, deltay = 0;
		while (deltax == 0 && deltay == 0) {
			deltax = RPG.pick(NUDGES);
			deltay = RPG.pick(NUDGES);
		}
		int tox = x + deltax;
		int toy = y + deltay;
		if (!World.validatecoordinate(tox, toy)
				|| (!Terrain.WATER.equals(Terrain.current())
						&& Terrain.WATER.equals(Terrain.get(tox, toy)))) {
			displace();
			return;
		}
		ArrayList<WorldActor> actors = WorldActor.getall();
		actors.remove(this);
		if (tox >= 0 && toy >= 0 && tox < World.MAPDIMENSION
				&& toy < World.MAPDIMENSION
				&& WorldActor.get(tox, toy, actors) == null) {
			move(tox, toy);
		} else {
			displace();
		}
	}

	/**
	 * Called on each instance once per day.
	 * 
	 * @param time
	 *            Current hour, starting from hour zero at the beggining of the
	 *            game.
	 */
	public void turn(long time, WorldScreen world) {
		// nothing by default
	}

	/**
	 * Called when a {@link Squad} enters the same world square as this actor.
	 * 
	 * @return <code>true</code> if the {@link World} should react after this
	 *         interaction.
	 * @throws RepeatTurn
	 */
	public boolean interact() {
		return false;
	}

	/**
	 * Note that this doesn't {@link #place()} or update the actor in any way.
	 * 
	 * @param x
	 *            World coordinate.
	 * @param y
	 *            World coordinate.
	 */
	public void setlocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return Distance from this actor to the given coordinates.
	 */
	public double distance(int xp, int yp) {
		return Walker.distance(xp, yp, x, y);
	}

	/**
	 * @return Actor of the given type that occupies the given coordinates, or
	 *         <code>null</code>.
	 */
	public static WorldActor get(int x, int y,
			Class<? extends WorldActor> type) {
		return get(x, y, getall(type));
	}

	/**
	 * @return Any actor on these coordinates.
	 */
	public static WorldActor get(int x, int y) {
		return get(x, y, getall());
	}

	/**
	 * @return Actor of the given set that occupies these coordinates.
	 */
	public static WorldActor get(int x, int y,
			List<? extends WorldActor> actors) {
		for (WorldActor actor : actors) {
			if (actor.x == x && actor.y == y) {
				return actor;
			}
		}
		return null;
	}

	/**
	 * @return A new list with all existing {@link WorldActor}s.
	 */
	public static ArrayList<WorldActor> getall() {
		ArrayList<WorldActor> actors = new ArrayList<WorldActor>();
		for (ArrayList<WorldActor> instances : INSTANCES.values()) {
			if (instances.isEmpty() || instances.get(0) instanceof Squad) {
				continue;
			}
			actors.addAll(instances);
		}
		actors.addAll(getall(Squad.class));
		return actors;
	}

	/**
	 * Note that this returns the canonical list from {@link #INSTANCES}.
	 * 
	 * @return All actors of the given type.
	 */
	public static ArrayList<WorldActor>
			getall(Class<? extends WorldActor> type) {
		ArrayList<WorldActor> all = INSTANCES.get(type);
		if (all == null) {
			all = new ArrayList<WorldActor>();
			INSTANCES.put(type, all);
		}
		return all;
	}

	/**
	 * @return The given realm color will be drawn on the {@link WorldScreen}.
	 *         <code>null</code> means no overlay.
	 * @see Realm#getawtcolor()
	 */
	public Realm getrealmoverlay() {
		return realm;
	}

	/**
	 * @return Any combatants that are situated in this actor, may return
	 *         <code>null</code> if this is not a valid request for this type of
	 *         actor. For performance if a {@link List} is returned it should be
	 *         the canonical one, and thus should not be directly modified by
	 *         the receiver.
	 * @see Combatant#newid()
	 */
	abstract public List<Combatant> getcombatants();
}
