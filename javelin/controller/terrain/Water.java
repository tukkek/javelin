package javelin.controller.terrain;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Ice;
import javelin.controller.terrain.hazard.Storm;
import javelin.controller.terrain.map.Maps;
import javelin.model.unit.Squad;
import javelin.model.world.ParkedVehicle;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.Town;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;

/**
 * Can only be trespassed by flying or swimming units or boat.
 * 
 * TODO water combats
 * 
 * TODO water vehicle
 * 
 * @author alex
 */
public class Water extends Terrain {
	private static final int DESERTRADIUS = 2;
	private static final int[] DELTAS = new int[] { +1, -1 };
	private Point currentheight;

	/** Constructor. */
	public Water() {
		this.name = "aquatic";
		this.difficulty = 0;
		this.difficultycap = -3;
		this.speedtrackless = 1f;
		this.speedroad = 1f;
		this.speedhighway = 1f;
		this.visionbonus = 0;
		representation = '~';
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new javelin.controller.terrain.map.Water());
		return m;
	}

	@Override
	protected Point generatesource(World world) {
		Point source = super.generatesource(world);
		while (!world.map[source.x][source.y].equals(Terrain.MOUNTAINS)
				&& !world.map[source.x][source.y].equals(Terrain.HILL)
				&& checkadjacent(source, DESERT, world, DESERTRADIUS) == 0) {
			source = super.generatesource(world);
		}
		currentheight = source;
		return source;
	}

	@Override
	protected Point expand(HashSet<Point> area, World world, Point p) {
		Point to = null;
		expansion: while (to == null) {
			to = new Point(p.x, p.y);
			if (RPG.r(1, 2) == 1) {
				to.x += RPG.pick(DELTAS);
			} else {
				to.y += RPG.pick(DELTAS);
			}
			if (checkinvalid(world, to.x, to.y)
					|| checkadjacent(to, DESERT, world, DESERTRADIUS) > 0) {
				to = null;
				World.retry();
				continue expansion;
			}
			for (WorldActor t : WorldActor.getall(Town.class)) {
				if (t.distance(to.x, to.y) <= 2) {
					to = null;
					World.retry();
					continue expansion;
				}
			}
		}
		currentheight = to;
		return currentheight;
	}

	@Override
	protected boolean generatetile(Terrain terrain, World w) {
		Terrain current = w.map[currentheight.x][currentheight.y];
		if (terrain.equals(Terrain.MOUNTAINS)
				&& !current.equals(Terrain.MOUNTAINS)) {
			return false;
		}
		if (terrain.equals(Terrain.HILL) && (!current.equals(Terrain.MOUNTAINS)
				&& !current.equals(Terrain.HILL))) {
			return false;
		}
		return true;
	}

	@Override
	protected Point generatereference(Point source, Point current) {
		return current;
	}

	@Override
	protected int generateareasize() {
		return super.generateareasize() / 2;
	}

	@Override
	public boolean generatetown(Point p, World w) {
		return false;
	}

	@Override
	public boolean enter(int x, int y) {
		return Squad.active.swim()
				|| WorldActor.get(x, y, ParkedVehicle.class) != null;
	}

	@Override
	public Set<Hazard> gethazards(boolean special) {
		Set<Hazard> hazards = super.gethazards(special);
		hazards.add(new Storm());
		if (special) {
			hazards.add(new Ice());
		}
		return hazards;
	}
}