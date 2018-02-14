package javelin.controller.generator.dungeon.template;

import java.util.ArrayList;

import javelin.controller.Point;
import tyrant.mikera.engine.RPG;

public abstract class Direction {
	public static final Direction NORTH = new Direction("North", 0, +1) {
		@Override
		ArrayList<Point> getborder(Template t) {
			ArrayList<Point> border = new ArrayList<Point>();
			for (int x = 0; x < t.width; x++) {
				border.add(new Point(x, t.height - 1));
			}
			return border;
		}

		@Override
		public Point connect(Point cursor, Template from, Template to,
				Point fromdoor, Point todoor) {
			cursor.x += fromdoor.x - todoor.x;
			cursor.y += from.height;
			return cursor;
		}
	};
	public static final Direction SOUTH = new Direction("South", 0, -1) {
		@Override
		ArrayList<Point> getborder(Template t) {
			ArrayList<Point> border = new ArrayList<Point>();
			for (int x = 0; x < t.width; x++) {
				border.add(new Point(x, 0));
			}
			return border;
		}

		@Override
		public Point connect(Point cursor, Template from, Template to,
				Point fromdoor, Point todoor) {
			cursor.x += fromdoor.x - todoor.x;
			cursor.y -= to.height;
			return cursor;
		}
	};
	public static final Direction EAST = new Direction("East", +1, 0) {
		@Override
		ArrayList<Point> getborder(Template t) {
			ArrayList<Point> border = new ArrayList<Point>();
			for (int y = 0; y < t.height; y++) {
				border.add(new Point(t.width - 1, y));
			}
			return border;
		}

		@Override
		public Point connect(Point cursor, Template from, Template to,
				Point fromdoor, Point todoor) {
			cursor.x += from.width;
			cursor.y += fromdoor.y - todoor.y;
			return cursor;
		}
	};
	public static final Direction WEST = new Direction("West", -1, 0) {
		@Override
		ArrayList<Point> getborder(Template t) {
			ArrayList<Point> border = new ArrayList<Point>();
			for (int y = 0; y < t.height; y++) {
				border.add(new Point(0, y));
			}
			return border;
		}

		@Override
		public Point connect(Point cursor, Template from, Template to,
				Point fromdoor, Point todoor) {
			cursor.x -= to.width;
			cursor.y += fromdoor.y - todoor.y;
			return cursor;
		}
	};
	public static final Direction[] DIRECTIONS = new Direction[] { NORTH, SOUTH,
			WEST, EAST };

	public String name;
	public Point reverse;
	public Point following;

	private Direction(String name, int x, int y) {
		this.name = name;
		following = new Point(x, y);
		reverse = new Point(-x, -y);
	}

	abstract ArrayList<Point> getborder(Template t);

	public static Direction getrandom() {
		return DIRECTIONS[RPG.r(0, 3)];
	}

	@Override
	public String toString() {
		return name;
	}

	public static Direction opposite(Direction d) {
		assert d != null;
		if (d == NORTH) {
			return SOUTH;
		}
		if (d == SOUTH) {
			return NORTH;
		}
		if (d == EAST) {
			return WEST;
		}
		return EAST;
	}

	public abstract Point connect(Point cursor, Template from, Template to,
			Point fromdoor, Point todoor);
}