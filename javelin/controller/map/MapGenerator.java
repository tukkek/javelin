package javelin.controller.map;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.map.desert.Rocky;
import javelin.controller.map.desert.Sandy;
import javelin.controller.map.desert.Tundra;
import javelin.controller.map.forest.DenseForest;
import javelin.controller.map.forest.Forest;
import javelin.controller.map.forest.SparseForest;
import javelin.controller.map.hill.GentleHiill;
import javelin.controller.map.hill.RuggedHill;
import javelin.controller.map.marsh.Moor;
import javelin.controller.map.marsh.Swamp;
import javelin.controller.map.mountain.ForbiddingMountain;
import javelin.controller.map.mountain.Meadow;
import javelin.controller.map.mountain.RuggedMountain;
import javelin.controller.map.plain.Battlefield;
import javelin.controller.map.plain.Farm;
import javelin.controller.map.plain.Grasslands;
import javelin.model.world.WorldMap;
import tyrant.mikera.engine.RPG;

/**
 * Selects and generates a map for a battle.
 * 
 * @author alex
 */
public class MapGenerator {
	/** TODO "dark tower" doesn't work - try to fix it? */
	static public Map generatebattlemap(int tile, boolean dungeon) {
		if (Javelin.DEBUGMAPTYPE != null) {
			Javelin.DEBUGMAPTYPE.generate();
			return Javelin.DEBUGMAPTYPE;
		}
		Map[] selection;
		if (dungeon) {
			selection = new Map[] { new TyrantMap("caves", Weather.DRY) };
		} else if (tile == WorldMap.EASY) {
			selection = new Map[] { wanderplain(), wanderplain(), wanderhill(),
					new TyrantMap("graveyard") };
		} else if (tile == WorldMap.MEDIUM) {
			selection = new Map[] { wanderforest(), wanderforest(),
					wanderhill(), new TyrantMap("dark forest") };
		} else if (tile == WorldMap.HARD) {
			selection = new Map[] { wandermoutain(), wanderdesert() };
		} else if (tile == WorldMap.VERYHARD) {
			selection = new Map[] { new TyrantMap("goblin village", 1),
					new TyrantMap("ruin"), wandermarsh(), wandermarsh(), };
		} else {
			throw new RuntimeException("[MapGenerator] Unknown tile " + tile);
		}
		return selection[RPG.r(0, selection.length - 1)];
	}

	private static Map wanderdesert() {
		Maps m = new Maps();
		m.add(new Tundra());
		m.add(new Rocky());
		m.add(new Sandy());
		return m.pick();
	}

	private static Map wanderforest() {
		Maps m = new Maps();
		m.add(new SparseForest());
		m.add(new Forest());
		m.add(new DenseForest());
		return m.pick();
	}

	private static Map wanderhill() {
		Maps m = new Maps();
		m.add(new GentleHiill());
		m.add(new RuggedHill());
		return m.pick();
	}

	private static Map wandermarsh() {
		Maps m = new Maps();
		m.add(new Moor());
		m.add(new Swamp());
		return m.pick();
	}

	private static Map wanderplain() {
		Maps m = new Maps();
		m.add(new Farm());
		m.add(new Grasslands());
		m.add(new Battlefield());
		return m.pick();
	}

	private static Map wandermoutain() {
		Maps m = new Maps();
		m.add(new Meadow());
		m.add(new RuggedMountain());
		m.add(new ForbiddingMountain());
		return m.pick();
	}
}
