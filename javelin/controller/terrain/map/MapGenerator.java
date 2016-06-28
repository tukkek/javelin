package javelin.controller.terrain.map;

import javelin.controller.db.Preferences;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Selects and generates a map for a battle.
 * 
 * @author alex
 */
public class MapGenerator {
	/** TODO "dark tower" doesn't work - try to fix it? */
	static public Map generatebattlemap(Terrain t, boolean dungeon) {
		if (Preferences.DEBUGMAPTYPE != null) {
			try {
				Map m = (Map) Class
						.forName(MapGenerator.class.getPackage().getName() + "."
								+ Preferences.DEBUGMAPTYPE)
						.newInstance();
				return m;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(
						"Cannot load map: " + Preferences.DEBUGMAPTYPE);
			}
		}
		return (dungeon ? Dungeon.getmaps() : t.getmaps()).pick();
	}
}
