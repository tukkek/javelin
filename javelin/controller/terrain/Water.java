package javelin.controller.terrain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.WorldGenerator;
import javelin.controller.map.Maps;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Ice;
import javelin.controller.terrain.hazard.Storm;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.ParkedVehicle;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
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
	private static final int[] DELTAS = new int[] { +1, -1 };
	private Point currentheight;

	/** Constructor. */
	public Water() {
		name = "aquatic";
		difficulty = 0;
		difficultycap = -3;
		speedtrackless = 1f;
		speedroad = 1f;
		speedhighway = 1f;
		visionbonus = 0;
		representation = '~';
		liquid = true;
	}

	@Override
	public Maps getmaps() {
		Maps m = new Maps();
		m.add(new javelin.controller.map.terrain.Water());
		return m;
	}

	@Override
	protected Point generatesource(World world) {
		Point source = super.generatesource(world);
		while (!world.map[source.x][source.y].equals(Terrain.MOUNTAINS)
				&& !world.map[source.x][source.y].equals(Terrain.HILL)
				&& search(source, DESERT, World.scenario.desertradius,
						world) == 0) {
			source = super.generatesource(world);
		}
		currentheight = source;
		return source;
	}

	@Override
	protected Point expand(HashSet<Point> area, World world) {
		List<Point> pool = new ArrayList<Point>(area);
		Point to = null;
		while (to == null) {
			to = expandto(RPG.pick(pool), world);
			WorldGenerator.retry();
		}
		currentheight = to;
		return currentheight;
	}

	Point expandto(Point p, World w) {
		Point to = new Point(p);
		if (RPG.chancein(2)) {
			to.x += RPG.pick(DELTAS);
		} else {
			to.y += RPG.pick(DELTAS);
		}
		if (checkinvalid(w, to.x, to.y)
				|| search(to, DESERT, World.scenario.desertradius, w) > 0) {
			return null;
		}
		for (Town t : Town.gettowns()) {
			if (t.distance(to.x, to.y) <= 2) {
				return null;
			}
		}
		return to;
	}

	@Override
	protected boolean generatetile(Terrain terrain, World w) {
		Terrain current = w.map[currentheight.x][currentheight.y];
		if (terrain.equals(Terrain.MOUNTAINS)
				&& !current.equals(Terrain.MOUNTAINS)) {
			return false;
		}
		if (terrain.equals(Terrain.HILL) && !current.equals(Terrain.MOUNTAINS)
				&& !current.equals(Terrain.HILL)) {
			return false;
		}
		return true;
	}

	@Override
	protected Point generatereference(Point source, Point current) {
		return current;
	}

	@Override
	protected int getareasize() {
		return super.getareasize() / 2;
	}

	@Override
	public boolean enter(int x, int y) {
		if (Squad.active.swim()) {
			return true;
		}
		Actor a = World.get(x, y);
		if (a == null) {
			return false;
		}
		ParkedVehicle v = a instanceof ParkedVehicle ? (ParkedVehicle) a : null;
		if (v != null && (v.transport.flies || v.transport.sails)) {
			return true;
		}
		Location l = a instanceof Location ? (Location) a : null;
		if (l != null && !l.allowentry) {
			return true;
		}
		return false;
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