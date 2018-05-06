package javelin.controller.terrain;

import javelin.controller.map.Map;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.underground.BigCave;
import javelin.controller.map.terrain.underground.Caves;
import javelin.controller.map.terrain.underground.Complex;
import javelin.controller.map.terrain.underground.Floor;
import javelin.controller.map.terrain.underground.Maze;
import javelin.controller.map.terrain.underground.Pit;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.Images;

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
		Maps maps = new Maps();
		for (Map m : new Map[] { new Caves(), new BigCave(), new Maze(),
				new Pit(), new Floor(), new Complex() }) {
			if (Dungeon.active != null) {
				m.floor = Images.getImage(Dungeon.active.floor);
				m.wall = Images.getImage(Dungeon.active.wall);
			}
			maps.add(m);
		}
		return maps;
	}
}
