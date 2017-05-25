package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.WorldBuilder;
import javelin.controller.terrain.Terrain;
import javelin.model.controller.scenario.Scenario;
import javelin.model.unit.Squad;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldTile;
import javelin.view.screen.WorldScreen;

/**
 * Game world overview. This is focused on generating the initial game state.
 * 
 * TODO would be nice to have tiles reflect the official d20 terrains (add
 * desert and hill)
 * 
 * @see WorldScreen
 * @author alex
 */
public class World implements Serializable {
	/** Ruleset of the current game. */
	public static Scenario scenario;
	/** Randomly generated world map. */
	public static World seed = null;
	/** Facilitate movement. */
	public boolean[][] roads;
	/** Upgraded {@link #roads}. */
	public boolean[][] highways;
	/** Map of terrain tiles by [x][y] coordinates. */
	public Terrain[][] map;

	/** Contains all actor instances still in the game. */
	public final HashMap<Class<? extends Actor>, ArrayList<Actor>> actors = new HashMap<Class<? extends Actor>, ArrayList<Actor>>();
	public final ArrayList<String> townnames = new ArrayList<String>();
	/**
	 * Intermediary for {@link WorldTile} while loading.
	 * 
	 * TODO clean?
	 * 
	 * @see Tile#discovered
	 */
	public final HashSet<Point> discovered = new HashSet<Point>();

	public World() {
		map = new Terrain[scenario.size][scenario.size];
		initroads();
		initnames();
	}

	void initroads() {
		int size = scenario.size;
		roads = new boolean[size][size];
		highways = new boolean[size][size];
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				roads[x][y] = false;
				highways[x][y] = false;
			}
		}
	}

	/**
	 * @return <code>true</code> if given coordinates are within the world map.
	 */
	public static boolean validatecoordinate(int x, int y) {
		return 0 <= x && x < scenario.size && 0 <= y && y < scenario.size;
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = 0; y < scenario.size; y++) {
			for (int x = 0; x < scenario.size; x++) {
				s += map[x][y].representation;
			}
			s += "\n";
		}
		return s;
	}

	/**
	 * Populates {@link NAMES}. This may be needed if restarting {@link World}
	 * generation.
	 *
	 * @see retry
	 */
	public void initnames() {
		townnames.clear();
		townnames.add("Alexandria"); // my name :)
		townnames.add("Lindblum"); // final fantasy 9
		townnames.add("Sigil"); // planescape: torment
		townnames.add("Reno");// fallout 2
		townnames.add("Marrymore");// super mario rpg
		townnames.add("Kakariko"); // zelda
		townnames.add("The Citadel"); // mass effect
		townnames.add("Tristam");// diablo
		townnames.add("Midgar"); // final fantasy 7
		townnames.add("Medina");// chrono trigger
		townnames.add("Figaro"); // final fantasy 6
		townnames.add("Balamb"); // final fantasy 8
		townnames.add("Zanarkand"); // final fantasy 10
		townnames.add("Cornelia"); // final fantasy 1
		townnames.add("Vivec");// morrowind
		townnames.add("Termina");// chrono cross
		townnames.add("Tarant");// arcanum
		Collections.shuffle(townnames);
	}

	/**
	 * Note that this returns the canonical list from {@link World#actors}.
	 *
	 * @return All actors of the given type.
	 */
	public static ArrayList<Actor> getall(Class<? extends Actor> type) {
		ArrayList<Actor> all = getseed().actors.get(type);
		if (all == null) {
			all = new ArrayList<Actor>();
			getseed().actors.put(type, all);
		}
		return all;
	}

	/**
	 * @return A new list with all existing {@link Actor}s.
	 */
	public static ArrayList<Actor> getall() {
		ArrayList<Actor> actors = new ArrayList<Actor>();
		for (ArrayList<Actor> instances : getseed().actors.values()) {
			if (instances.isEmpty() || instances.get(0) instanceof Squad) {
				continue;
			}
			actors.addAll(instances);
		}
		actors.addAll(World.getall(Squad.class));
		return actors;
	}

	/**
	 * @return Actor of the given set that occupies these coordinates.
	 */
	public static Actor get(int x, int y, List<? extends Actor> actors) {
		for (Actor actor : actors) {
			if (actor.x == x && actor.y == y) {
				return actor;
			}
		}
		return null;
	}

	/**
	 * @return Any actor on these coordinates.
	 */
	public static Actor get(int x, int y) {
		return World.get(x, y, World.getall());
	}

	/**
	 * @return Actor of the given type that occupies the given coordinates, or
	 *         <code>null</code>.
	 */
	public static Actor get(int x, int y, Class<? extends Actor> type) {
		return World.get(x, y, World.getall(type));
	}

	/**
	 * Needs to be called during world building, as each {@link WorldBuilder}
	 * thread has a different world. During normal gameplay, {@link #seed} can
	 * be accessed directly.
	 * 
	 * TODO make sure this is only being used where necessary, to avoid the
	 * overhead
	 * 
	 * @return If {@link #building}, the thread-relevant world instance,
	 *         otherwise {@link #seed}.
	 */
	public static World getseed() {
		Thread t = Thread.currentThread();
		if (t instanceof WorldBuilder) {
			return ((WorldBuilder) t).world;
		}
		return seed;
	}
}
