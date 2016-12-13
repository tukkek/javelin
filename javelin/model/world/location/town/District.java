package javelin.model.world.location.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Construction;
import javelin.model.world.location.Location;

/**
 * This class represents the {@link Location}s that can be found within a city's
 * area. Since this search can be a relatively costly operation if performed
 * repeatedly this serves as a discardable cache to be used within such an
 * operations.
 * 
 * Instead of performing a full search of the relevant data upon creation, data
 * is only gathered when needed and then cached accordingly.
 * 
 * @author alex
 */
public class District {
	static final int MOSTNEIGHBORSALLOWED = 1;

	public Town town;
	ArrayList<Location> locations = null;
	HashSet<Point> area = null;
	ArrayList<Squad> squads = null;

	public District(Town t) {
		this.town = t;
	}

	public ArrayList<Location> getlocations() {
		if (locations != null) {
			return locations;
		}
		locations = new ArrayList<Location>();
		for (Point p : getarea()) {
			WorldActor a = WorldActor.get(p.x, p.y);
			if (a != null && a instanceof Location) {
				locations.add((Location) a);
			}
		}
		return locations;
	}

	public HashSet<Point> getarea() {
		if (area != null) {
			return area;
		}
		int radius = getradius();
		area = new HashSet<Point>();
		for (int x = town.x - radius; x <= town.x + radius; x++) {
			for (int y = town.y - radius; y <= town.y + radius; y++) {
				if (World.validatecoordinate(x, y)) {
					area.add(new Point(x, y));
				}
			}
		}
		return area;
	}

	public int getradius() {
		return town.getrank() + 1;
	}

	/**
	 * @param type
	 *            Will check for this exact class, not subclasses.
	 * @return Location of the given type or <code>null</code> if none was
	 *         found.
	 */
	public Location getlocation(Class<? extends Location> type) {
		for (Location l : getlocations()) {
			if (l.getClass().equals(type)) {
				return l;
			}
		}
		return null;
	}

	public ArrayList<Location> getlocationtype(Class<? extends Location> type) {
		ArrayList<Location> result = new ArrayList<Location>();
		for (Location l : getlocations()) {
			if (type.isInstance(l)) {
				result.add(l);
			}
		}
		return result;
	}

	public ArrayList<Squad> getsquads() {
		if (squads != null) {
			return squads;
		}
		getarea();
		squads = new ArrayList<Squad>();
		for (Squad s : Squad.getsquads()) {
			if (area.contains(new Point(s.x, s.y))) {
				squads.add(s);
			}
		}
		return squads;
	}

	/**
	 * @return All spots that can be built on and do not have too many neighbors
	 *         (as to prevent the creation of "walls" {@link Squad}s will have
	 *         trouble passing through). This list is shuffled by default.
	 */
	public ArrayList<Point> getfreespaces() {
		ArrayList<WorldActor> actors = WorldActor.getall();
		ArrayList<WorldActor> locations = new ArrayList<WorldActor>();
		for (WorldActor a : actors) {
			if (a instanceof Location) {
				locations.add(a);
			}
		}
		ArrayList<Point> free = new ArrayList<Point>();
		searching: for (Point p : getarea()) {
			if (Terrain.get(p.x, p.y).equals(Terrain.WATER)
					|| WorldActor.get(p.x, p.y, actors) != null) {
				continue searching;
			}
			int neighbors = 0;
			for (int x = p.x - 1; x <= p.x + 1; x++) {
				for (int y = p.y - 1; y <= p.y + 1; y++) {
					if ((x == p.x && y == p.y)
							|| !World.validatecoordinate(x, y)
							|| World.roads[p.x][p.y] || World.highways[p.x][p.y]
							|| WorldActor.get(x, y, locations) == null) {
						continue;
					}
					neighbors += 1;
					if (neighbors > MOSTNEIGHBORSALLOWED) {
						continue searching;
					}
				}
			}
			free.add(p);
		}
		Collections.shuffle(free);
		return free;
	}

	public boolean isbuilding(Class<? extends Location> site) {
		for (Location l : getlocationtype(Construction.class)) {
			Construction c = (Construction) l;
			if (site.isInstance(c.goal)) {
				return true;
			}
		}
		return false;
	}
}
