package javelin.model.world;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javelin.controller.WorldBuilder;
import javelin.controller.terrain.Terrain;
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
	/**
	 * Map size in squares.
	 */
	static public int SIZE = 30;
	/**
	 * Randomly generated world map.
	 */
	public static World seed = null;
	/** Facilitate movement. */
	public final boolean[][] roads = new boolean[SIZE][SIZE];
	/** Upgraded {@link #roads}. */
	public final boolean[][] highways = new boolean[SIZE][SIZE];
	/** Map of terrain tiles by [x][y] coordinates. */
	public final Terrain[][] map = new Terrain[SIZE][SIZE];
	/** Contains all actor instances still in the game. */
	public final HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>> actors = new HashMap<Class<? extends WorldActor>, ArrayList<WorldActor>>();
	public final ArrayList<String> townnames = new ArrayList<String>();

	public World() {
		initroads();
		initnames();
	}

	void initroads() {
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				roads[x][y] = false;
				highways[x][y] = false;
			}
		}
	}

	/**
	 * @return <code>true</code> if given coordinates are within the world map.
	 */
	public static boolean validatecoordinate(int x, int y) {
		return 0 <= x && x < SIZE && 0 <= y && y < SIZE;
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
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
		if (seed != null) {
			return seed;
		}
		WorldBuilder builder = (WorldBuilder) Thread.currentThread();
		return builder.world;
	}
}
