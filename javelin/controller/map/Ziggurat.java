package javelin.controller.map;

import javelin.controller.fight.minigame.Run;
import javelin.controller.terrain.map.Map;

/**
 * Clear map divided into 5x5 segments (7 rows/columns).
 * 
 * @see Run
 * @author alex
 */
public class Ziggurat extends Map {
	/** Map size. */
	public static final int SIZE = Run.NSEGMENTS * Run.SEGMENTSIZE;

	/** Constructor. */
	public Ziggurat() {
		super("Ziggurat", SIZE, SIZE);
	}

	@Override
	public void generate() {
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				map[x][y].blocked = true;
			}
		}
	}
}
