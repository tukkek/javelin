package javelin.controller.terrain;

import java.util.Set;

import javelin.controller.Point;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.hill.Gentle;
import javelin.controller.map.terrain.hill.Rugged;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Rockslide;
import javelin.model.world.World;

/**
 * Similar to {@link Plains}.
 * 
 * @author alex
 */
public class Hill extends Terrain {
	/** Constructor. */
	public Hill() {
		this.name = "hill";
		this.difficulty = -1;
		this.difficultycap = -4;
		this.speedtrackless = 1 / 2f;
		this.speedroad = 3 / 4f;
		this.speedhighway = 1f;
		this.visionbonus = +2;
		representation = '^';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Gentle());
		m.add(new Rugged());
		return m;
	}

	@Override
	protected Point generatesource(World w) {
		Point source = super.generatesource(w);
		while (!w.map[source.x][source.y].equals(Terrain.FOREST)
				|| (search(source, Terrain.MOUNTAINS, 1, w) == 0
						&& search(source, Terrain.PLAIN, 1, w) == 0)) {
			source = super.generatesource(w);
		}
		return source;
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		if (special) {
			hazards.add(new Rockslide());
		}
		return hazards;
	}
}