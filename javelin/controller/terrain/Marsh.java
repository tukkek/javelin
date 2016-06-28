package javelin.controller.terrain;

import java.util.Set;

import javelin.controller.terrain.hazard.Break;
import javelin.controller.terrain.hazard.Cold;
import javelin.controller.terrain.hazard.Flood;
import javelin.controller.terrain.hazard.GettingLost;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.map.Maps;
import javelin.controller.terrain.map.marsh.Moor;
import javelin.controller.terrain.map.marsh.Swamp;
import javelin.model.world.World;
import tyrant.mikera.engine.Point;

/**
 * Bog, swamp.
 * 
 * @author alex
 */
public class Marsh extends Terrain {
	/** Constructor. */
	public Marsh() {
		this.name = "marsh";
		this.difficulty = +2;
		this.difficultycap = -1;
		this.speedtrackless = 1 / 2f;
		this.speedroad = 3 / 4f;
		this.speedhighway = 1f;
		this.visionbonus = -2;
		representation = 'm';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new Moor());
		m.add(new Swamp());
		return m;
	}

	@Override
	protected Point generatesource(World w) {
		Point source = super.generatesource(w);
		while (!w.map[source.x][source.y].equals(Terrain.FOREST)
				&& checkadjacent(source, WATER, w, 1) == 0) {
			source = super.generatesource(w);
		}
		return source;
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		hazards.add(new GettingLost(10));
		hazards.add(new Cold());
		if (special) {
			hazards.add(new Flood());
			hazards.add(new Break());
		}
		return hazards;
	}
}