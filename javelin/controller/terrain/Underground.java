package javelin.controller.terrain;

import javelin.controller.terrain.map.Maps;
import javelin.controller.terrain.map.underground.BigCave;
import javelin.controller.terrain.map.underground.Caves;
import javelin.controller.terrain.map.underground.Floor;
import javelin.controller.terrain.map.underground.Maze;
import javelin.controller.terrain.map.underground.Pit;

/**
 * See {@link Terrain#UNDERGROUND}.
 * 
 * @author alex
 */
public class Underground extends Terrain {
	/** Constructor. */
	public Underground() {
		name = "underground";
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Caves());
		m.add(new BigCave());
		m.add(new Maze());
		m.add(new Pit());
		m.add(new Floor());
		return m;
	}
}
