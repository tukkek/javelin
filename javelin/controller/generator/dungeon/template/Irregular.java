package javelin.controller.generator.dungeon.template;

import java.util.Collections;
import java.util.LinkedList;

import javelin.controller.Point;
import tyrant.mikera.engine.RPG;

public class Irregular extends Template {
	private static final Point[] ADJACENT = new Point[] { new Point(-1, 0),
			new Point(+1, 0), new Point(0, -1), new Point(0, +1) };
	private static final int PERCENTMIN = 30;
	private static final int PERCENTMAX = 60;

	@Override
	public void generate() {
		width = 0;
		while (width < 3 || height < 3) {
			initrandom();
		}
		double ratio = RPG.r(PERCENTMIN, PERCENTMAX) / 100.0;
		LinkedList<Point> expand = getborders();
		Collections.shuffle(expand);
		for (int i = 0; i < expand.size() * ratio; i++) {
			Point border = expand.get(i);
			tiles[border.x][border.y] = WALL;
		}
		for (int count = count(WALL); !expand.isEmpty()
				&& count < getarea() * ratio; count = count(WALL)) {
			Point p = RPG.pick(expand);
			expand.remove(p);
			if (checkblock(p)) {
				continue;
			}
			tiles[p.x][p.y] = WALL;
			for (Point adjacent : new Point[] { new Point(p.x - 1, p.y),
					new Point(p.x + 1, p.y), new Point(p.x, p.y - 1),
					new Point(p.x, p.y + 1) }) {
				if (adjacent.validate(0, 0, width, height)
						&& tiles[adjacent.x][adjacent.y] != WALL) {
					expand.add(adjacent);
				}
			}
		}
	}

	void initrandom() {
		init(RPG.r(3, 7), RPG.r(1, 6));
	}

	boolean checkblock(Point p) {
		int walls = 0;
		for (Point wall : ADJACENT) {
			wall = new Point(p.x + wall.x, p.y + wall.y);
			if (!wall.validate(0, 0, width, height)
					|| tiles[wall.x][wall.y] == WALL) {
				walls += 1;
			}
		}
		return walls >= 2;
	}

	LinkedList<Point> getborders() {
		final LinkedList<Point> borders = new LinkedList<Point>();
		iterate(new Iterator() {
			@Override
			public void iterate(TemplateTile t) {
				if (isborder(t.x, t.y)) {
					borders.add(new Point(t.x, t.y));
				}
			}

		});
		return borders;
	}
}
