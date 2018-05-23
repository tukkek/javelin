package javelin.controller.terrain;

import javelin.controller.map.Map;
import javelin.controller.map.Maps;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.underground.BigCave;
import javelin.old.underground.Caves;
import javelin.old.underground.Complex;
import javelin.old.underground.Floor;
import javelin.old.underground.Maze;
import javelin.old.underground.Pit;
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
		difficulty = 0;
		difficultycap = Integer.MAX_VALUE;
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
