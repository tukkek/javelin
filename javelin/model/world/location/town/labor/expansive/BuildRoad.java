package javelin.model.world.location.town.labor.expansive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.controller.DistanceComparator;
import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.Portal;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;

/**
 * TODO need to consider A->B and B->A
 *
 * @author alex
 */
public class BuildRoad extends Labor {
	/**
	 * To prevent too lengthy searches since the algorhitm is currently
	 * unoptimized.
	 *
	 * TODO
	 */
	static final int MAXSEARCHDEPTH = 9;

	public class PathSearch {
		Point from;
		Town to;
		ArrayList<Point> result = null;
		ArrayList<WorldActor> locations = new ArrayList<WorldActor>();

		PathSearch(Town from, Town to) {
			if (swap(from, to)) {
				this.from = from.getlocation();
				this.to = to;
			} else {
				this.from = to.getlocation();
				this.to = from;
			}
			for (WorldActor a : WorldActor.getall()) {
				if (a instanceof Location && !(a instanceof Portal)) {
					locations.add(a);
				}
			}
		}

		/**
		 * This is a simple but important trick, ensuring simmetry across towns.
		 * This allows the building of roads from A->B exactly as B->A, enabling
		 * simultaneous building across 2 ends and also preventing from having a
		 * unique roas A<->B instead of two.
		 *
		 * The path is reversed in {@link #search()} as needed.
		 */
		boolean swap(Town from, Town to) {
			return from.x == to.x ? from.y > to.y : from.x > to.x;
		}

		ArrayList<Point> search() {
			search(from, new ArrayList<Point>());
			if (result == null || result.isEmpty()) {
				return null;
			}
			if (town.distanceinsteps(result.get(0)) > town
					.distanceinsteps(result.get(result.size() - 1))) {
				Collections.reverse(result);
			}
			return result;
		}

		void search(Point p, ArrayList<Point> partialpath) {
			if (result != null) {
				return;
			}
			if (p.equals(to.getlocation())) {
				result = partialpath;
				return;
			}
			if (partialpath.size() > MAXSEARCHDEPTH) {
				return;
			}
			if (p != from) {
				partialpath = new ArrayList<Point>(partialpath);
				partialpath.add(p);
			}
			for (Point step : getsteps(p)) {
				search(step, partialpath);
			}
		}

		/**
		 * TODO this is a really lazy brute-force implementation
		 */
		ArrayList<Point> getsteps(Point current) {
			final int currentdistance = to.distanceinsteps(current.x,
					current.y);
			final ArrayList<Point> steps = new ArrayList<Point>();
			for (int x = -1; x <= +1; x++) {
				for (int y = -1; y <= +1; y++) {
					if (x == 0 && y == 0) {
						continue;
					}
					final Point step = new Point(current.x + x, current.y + y);
					if (validate(step, currentdistance)) {
						steps.add(step);
					}
				}
			}
			steps.sort(new Comparator<Point>() {
				@Override
				public int compare(Point o1, Point o2) {
					return new Double(to.distance(o1.x, o1.y))
							.compareTo(to.distance(o2.x, o2.y));
				}
			});
			return steps;
		}

		boolean validate(Point step, int currentdistance) {
			if (!World.validatecoordinate(step.x, step.y)
					|| Terrain.get(step.x, step.y).equals(Terrain.WATER)
					|| to.distanceinsteps(step.x, step.y) >= currentdistance) {
				return false;
			}
			final WorldActor location = WorldActor.get(step.x, step.y,
					locations);
			return location == null || location == to;
		}
	}

	Town target = null;

	public BuildRoad() {
		this("Build road");
	}

	public BuildRoad(String name) {
		super(name, 0, Town.TOWN);
	}

	@Override
	protected void define() {
		ArrayList<Town> towns = Town.gettowns();
		towns.remove(town);
		towns.sort(new DistanceComparator(town));
		for (Town t : towns) {
			if (getpath(t) != null && !pathcomplete(t)) {
				target = t;
				break;
			}
		}
		if (target != null) {
			name = name(target);
			cost = getcost();
		}
	}

	int getcost() {
		int cost = 0;
		for (Point p : getpath(target)) {
			if (!hasroad(p)) {
				cost += getcost(p);
			}
		}
		return cost;
	}

	protected boolean hasroad(Point p) {
		return World.roads[p.x][p.y] || World.highways[p.x][p.y];
	}

	protected String name(Town target) {
		return "Build road to " + target;
	}

	ArrayList<Point> getpath(Town target) {
		return new PathSearch(town, target).search();
	}

	boolean pathcomplete(Town t) {
		return currenttile(t) == null;
	}

	Point currenttile(Town target) {
		ArrayList<Point> path = getpath(target);
		if (path != null) {
			for (Point p : path) {
				if (!hasroad(p)) {
					return p;
				}
			}
		}
		return null;
	}

	@Override
	public void done() {
		Point current = currenttile(target);
		while (current != null) {
			build(current);
			current = currenttile(target);
		}
	}

	protected void build(Point p) {
		World.roads[p.x][p.y] = true;
	}

	@Override
	public boolean validate(District d) {
		if (!super.validate(d) || town.population * 100 < cost) {
			return false;
		}
		for (Labor l : d.town.governor.getprojects()) {
			BuildRoad road = l != this && l instanceof BuildRoad ? (BuildRoad) l
					: null;
			if (road != null && road.target == target) {
				/* A road or highway is already being built to this town */
				return false;
			}
		}
		if (target == null
				|| WorldActor.get(target.x, target.y, Town.class) == null
				|| getpath(target) == null || pathcomplete(target)) {
			return false;
		}
		cost = getcost();
		return true;
	}

	@Override
	public void work(float step) {
		progress += step;
		Point p = currenttile(target);
		if (p == null) {
			done();
			cancel();
			return;
		}
		float cost = getcost(p);
		if (progress >= cost) {
			build(p);
			progress -= cost;
		}
	}

	@Override
	public int getprogress() {
		ArrayList<Point> path = getpath(target);
		if (path == null) {
			return 100;
		}
		int built = 0;
		for (Point step : path) {
			if (hasroad(step)) {
				built += 1;
			} else {
				break;
			}
		}
		return 100 * built / path.size();
	}

	protected float getcost(Point p) {
		return 7 / Terrain.get(p.x, p.y).getspeed(p.x, p.y);
	}
}
