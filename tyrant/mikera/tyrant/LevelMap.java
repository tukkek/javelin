/*
 * Created on 26-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import java.util.HashMap;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 * 
 *         Implements static methods for handling level maps
 */
public class LevelMap implements java.io.Serializable {
	private static final long serialVersionUID = 3545517309273125684L;
	// current pixels
	private int[] pixels = null;
	private int[] currentMemory = null;
	private BattleMap lastMap = null;
	private HashMap mapMemory = new HashMap();

	public static LevelMap instance() {
		LevelMap l = (LevelMap) Game.instance().get("MapMemory");
		if (l == null) {
			l = new LevelMap();
			Game.instance().set("MapMemory", l);
		}
		return l;
	}

	private HashMap getMapMemory() {
		return mapMemory;
	}

	private int[] getMapMemory(BattleMap m) {
		HashMap h = getMapMemory();
		int[] memory = (int[]) h.get(m);
		if (memory == null && m == lastMap) {
			memory = currentMemory;
		}
		if (memory == null) {
			memory = new int[m.width * m.height];
			if (!m.getFlag("ForgetMap")) {
				h.put(m, memory);
			}
		}
		currentMemory = memory;
		lastMap = m;
		return memory;
	}

	public static void reveal(BattleMap map) {
		int w = map.width;
		int h = map.height;
		int[] mem = instance().getMapMemory(map);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				setMapColor(map, mem, x, y);
			}
		}
	}

	public static void forget(BattleMap map, int chance) {
		if (map == null) {
			return;
		}
		int[] mem = instance().getMapMemory(map);

		for (int i = 0; i < mem.length; i++) {
			if (RPG.r(100) < chance) {
				mem[i] = 0;
			}
		}
	}

	public int[] getMapView(BattleMap map) {
		int w = map.width;
		int h = map.height;

		int[] mem = getMapMemory(map);

		Thing he = Game.hero();
		int r = Being.calcViewRange(he);

		for (int y = RPG.max(0, he.y - r); y < RPG.min(he.y + r + 1, h); y++) {
			for (int x = RPG.max(0, he.x - r); x < RPG.min(he.x + r + 1,
					w); x++) {
				if (map.isVisibleChecked(x + y * w)) {
					setMapColor(map, mem, x, y);
				}
			}
		}

		if (pixels == null || pixels.length != mem.length) {
			pixels = new int[mem.length];
		}

		System.arraycopy(mem, 0, pixels, 0, mem.length);

		for (int y = RPG.max(0, he.y - r); y < RPG.min(he.y + r + 1, h); y++) {
			for (int x = RPG.max(0, he.x - r); x < RPG.min(he.x + r + 1,
					w); x++) {
				updateMapColor(map, pixels, x, y);
			}
		}

		return pixels;
	}

	/**
	 * This method modifiers map memory colour Use this for temporary map
	 * colours e.g. Beings, Hero on radar
	 * 
	 * @param map
	 * @param pixels
	 * @param x
	 * @param y
	 */
	private static void updateMapColor(BattleMap map, int[] pixels, int x,
			int y) {
		int i = x + map.width * y;
		if (map.isVisibleChecked(i)) {
			int c = pixels[i] + 0x00101010;
			Thing t = map.getObjectsChecked(i);
			while (t != null) {
				if (t.getFlag("IsMobile")) {
					if (t.isHero()) {
						pixels[i] = 0x00FF8000;
						return;
						// } else if (Game.hero(),t)) {
						// c=0x00D00000;
					}
					c = 0x0000D020;
				}
				t = t.next;
			}
			pixels[i] = c;
		}
	}

	/**
	 * This method sets map memory colour according to map contents
	 * 
	 * @param map
	 * @param pixels
	 * @param x
	 * @param y
	 */
	private static void setMapColor(BattleMap map, int[] pixels, int x, int y) {

		// int tile = BattleScreen.active.gettile(x, y);
		// int c = Tile.getMapColour(tile);
		//
		// Thing t = map.getObjects(x, y);
		// while (t != null) {
		// int mc = t.getStat("MapColour");
		// if (mc > 0) {
		// c = mc;
		// }
		// t = t.next;
		// }
		//
		// pixels[x + map.width * y] = c;
	}

}
