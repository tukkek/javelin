package javelin.controller.terrain;

import javelin.controller.map.Maps;
import javelin.controller.map.terrain.underground.BigCave;
import javelin.controller.map.terrain.underground.Caves;
import javelin.controller.map.terrain.underground.Floor;
import javelin.controller.map.terrain.underground.Maze;
import javelin.controller.map.terrain.underground.Pit;

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
