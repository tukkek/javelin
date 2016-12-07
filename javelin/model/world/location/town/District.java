package javelin.model.world.location.town;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
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
	Town town;
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
		int rank = town.getrank();
		area = new HashSet<Point>();
		for (int x = town.x - rank; x <= town.x + rank; x++) {
			for (int y = town.y - rank; y <= town.y + rank; y++) {
				if (World.validatecoordinate(x, y)) {
					area.add(new Point(x, y));
				}
			}
		}
		return area;
	}

	/**
	 * @param class1
	 *            Will check for this exact class, not subclasses.
	 * @return Location of the given type or <code>null</code> if none was
	 *         found.
	 * @see #getdistrict()
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
}
