package javelin.model.world;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

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
	public static HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>> INSTANCES = new HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>>();
	static final int[] NUDGES = new int[] { -1, 0, +1 };
	/** x coordinate. */
	public int x = -1;
	/** y coordinate. */
	public int y = -1;
	/**
	 * Which team this actor belongs to or <code>null</code> if neutral. If
	 * <code>null</code> during construction, in which case it won't be updated
	 * by {@link #place()}.
	 */
	public Realm realm = null;
	/**
	 * If <code>true</code> this actor will be ignored by {@link Incursion}s.
	 */
	public boolean impermeable = false;

	/** Constructor. */
	public WorldActor() {
		// registerinstance();
	}

	/** Permanently removes this actor from the game. */
	public void remove() {
		deregisterinstance();
	}

	/** Adds this actor to the game. Should only be used once in theory. */
	public void place() {
		registerinstance();
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
		// if (x == -1 && !(this instanceof Squad)) {
		// throw new RuntimeException("Impossible #actor");
		// }
		ArrayList<WorldActor> list = INSTANCES.get(getClass());
		if (list == null) {
			list = new ArrayList<WorldActor>(1);
			INSTANCES.put(getClass(), list);
		}
		if (!list.contains(this)) {
			list.add(this);
		}
	}

	/** Removes this instance from {@link #INSTANCES}. */
	protected void deregisterinstance() {
		List<WorldActor> list = INSTANCES.get(getClass());
		if (list != null) {
			list.remove(this);
		}
	}

	public void displace(int depth) {
		if (depth == 50) {
			if (Javelin.DEBUG) {
				throw new RuntimeException("Too many calls to displace!");
			}
			return;
		}
		int deltax = 0, deltay = 0;
		while (deltax == 0 && deltay == 0) {
			deltax = RPG.pick(NUDGES);
			deltay = RPG.pick(NUDGES);
		}
		int tox = x + deltax;
		int toy = y + deltay;
		if (!World.validatecoordinate(tox, toy) || !cancross(tox, toy)) {
			displace(depth + 1);
			return;
		}
		ArrayList<WorldActor> actors = WorldActor.getall();
		actors.remove(this);
		if (tox >= 0 && toy >= 0 && tox < World.SIZE && toy < World.SIZE
				&& WorldActor.get(tox, toy, actors) == null) {
			move(tox, toy);
		} else {
			displace(depth + 1);
		}
	}

	protected boolean cancross(int tox, int toy) {
		final boolean to = Terrain.WATER.equals(Terrain.get(tox, toy));
		if (!to) {
			return true;
		}
		final boolean from = Terrain.WATER.equals(Terrain.get(x, y));
		return from && to;
	}

	/**
	 * Moves actor to nearby square until a free square is found.
	 */
	public void displace() {
		displace(0);
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
	public static ArrayList<WorldActor> getall(
			Class<? extends WorldActor> type) {
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

	/**
	 * @return Visual representation of this actor.
	 */
	abstract public Image getimage();

	/**
	 * @return Textual representation of this actor.
	 */
	abstract public String describe();

	/**
	 * @return <code>true</code> if both these actors are touching each other in
	 *         the {@link WorldScreen}.
	 */
	public boolean isadjacent(WorldActor active) {
		return Math.abs(x - active.x) <= 1 && Math.abs(y - active.y) <= 1;
	}

	public WorldActor getnearest(Class<? extends Location> targets) {
		WorldActor nearest = null;
		for (WorldActor p : getall(targets)) {
			if (p == this) {
				continue;
			}
			if (nearest == null
					|| distance(p.x, p.y) < distance(nearest.x, nearest.y)) {
				nearest = p;
			}
		}
		return nearest;
	}

	/**
	 * TODO ideally would be nice to chance all .x .y to .location (Point)
	 */
	public void setlocation(Point p) {
		setlocation(p.x, p.y);
	}

	public Point getlocation() {
		return new Point(x, y);
	}

	public int distanceinsteps(int xp, int yp) {
		return Math.max(Math.abs(xp - x), Math.abs(yp - y));
	}

	/**
	 * Note that this could return a hostile town!
	 *
	 * @return A {@link Town} district this location is part of or
	 *         <code>null</code> if it is not located inside one. If more than
	 *         one district encompasses this location, the one with the highest
	 *         {@link Town#population} will be returned.
	 */
	public District getdistrict() {
		ArrayList<Town> towns = Town.gettowns();
		District main = null;
		for (Town t : towns) {
			District d = t.getdistrict();
			if (distanceinsteps(t.x, t.y) <= d.getradius()
					&& (main == null || t.population > d.town.population)) {
				main = d;
			}
		}
		return main;
	}
}
