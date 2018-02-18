package javelin.controller.generator.dungeon.template.generated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.Direction;
import javelin.controller.generator.dungeon.template.Template;
import tyrant.mikera.engine.RPG;

public class Linear extends Template {
	static final float MAXDISTANCE = 1 / 3f;
	protected int minsize = 7;

	public Linear() {
		mutate = 0.5;
	}

	@Override
	public void generate() {
		setupsize();
		List<Point> borders = getdots();
		if (DungeonGenerator.DEBUG) {
			for (Point p : borders) {
				tiles[p.x][p.y] = '*';
			}
		}
		draw(new LinkedList<Point>(borders));
		fill();
	}

	void setupsize() {
		initrandom();
		while (width * height < minsize * minsize) {
			init(width + RPG.r(1, 4), height + RPG.r(1, 4));
		}
	}

	List<Point> getdots() {
		List<Point> borders = new ArrayList<Point>();
		for (Direction d : Direction.ALL) {
			borders.addAll(d.getborder(this));
		}
		int sides = Integer.MAX_VALUE;
		sides = RPG.r(Math.min(width, height), width + height);
		Collections.shuffle(borders);
		borders = borders.subList(0, sides);
		for (Point p : borders) {
			bump(p);
		}
		return borders;
	}

	void draw(LinkedList<Point> dots) {
		Point first = dots.pop();
		Point from = first;
		while (!dots.isEmpty()) {
			Point to = findclosest(dots, from);
			connect(from, to);
			dots.remove(to);
			from = to;
		}
		connect(from, first);
	}

	void fill() {
		for (Direction d : Direction.ALL) {
			for (Point outer : d.getborder(this)) {
				while (outer.validate(0, 0, width, height)
						&& tiles[outer.x][outer.y] == FLOOR) {
					tiles[outer.x][outer.y] = WALL;
					outer.x += d.reverse.x;
					outer.y += d.reverse.y;
				}
			}
		}
	}

	void connect(Point step, Point to) {
		step = step.clone();
		while (!step.equals(to)) {
			tiles[step.x][step.y] = WALL;
			if (step.x != to.x) {
				step.x += to.x > step.x ? +1 : -1;
			}
			if (step.y != to.y) {
				step.y += to.y > step.y ? +1 : -1;
			}
		}
	}

	Point findclosest(LinkedList<Point> dots, Point first) {
		Point closest = dots.get(0);
		double distance = closest.distance(first);
		for (int i = 1; i < dots.size(); i++) {
			Point p = dots.get(i);
			double distancep = p.distance(first);
			if (distancep < distance) {
				distance = distancep;
				closest = p;
			}
		}
		return closest;
	}

	protected void bump(Point p) {
		int maxdeltax = Math.round(width * MAXDISTANCE);
		int maxdeltay = Math.round(height * MAXDISTANCE);
		HashSet<Point> set = new HashSet<Point>();
		Point bump = null;
		while (bump == null || set.contains(bump)) {
			bump = p.clone();
			if (p.x == 0) {
				bump.x += RPG.r(0, maxdeltax);
			} else if (p.x == width - 1) {
				bump.x -= RPG.r(0, maxdeltax);
			} else if (p.y == 0) {
				bump.y += RPG.r(0, maxdeltay);
			} else {
				bump.x -= RPG.r(0, maxdeltay);
			}
		}
		if (bump.validate(0, 0, width, height)) {
			p.x = bump.x;
			p.y = bump.y;
		}
	}
}
