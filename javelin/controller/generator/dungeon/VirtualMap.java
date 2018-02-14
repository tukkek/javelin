package javelin.controller.generator.dungeon;

import java.util.ArrayList;
import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;

public class VirtualMap {
	class Room {
		int x;
		int y;
		int width;
		int height;

		public Room(int x, int y, int width, int height) {
			super();
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return x + ":" + y + " (" + width + "x" + height + ")";
		}
	}

	public ArrayList<Room> rooms = new ArrayList<Room>();
	public Character fill = ' ';

	HashMap<Point, Character> map = new HashMap<Point, Character>();

	public boolean draw(Template t, int rootx, int rooty, boolean check) {
		for (int x = 0; x < t.width; x++) {
			for (int y = 0; y < t.height; y++) {
				Point p = new Point(x + rootx, y + rooty);
				char c = t.tiles[x][y];
				if (!check) {
					map.put(p, c);
					continue;
				}
				Character old = map.get(p);
				if (old != null
						&& (old != Template.WALL || c != Template.WALL)) {
					return false;
				}
			}
		}
		return true;
	}

	public String rasterize(boolean commit) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		// int minx = Integer.MAX_VALUE;
		// int miny = Integer.MAX_VALUE;
		for (Point p : map.keySet()) {
			min = Math.min(min, Math.min(p.x, p.y));
			max = Math.max(max, Math.max(p.x, p.y));
			// minx = Math.min(minx, p.x);
			// miny = Math.min(miny, p.y);
		}
		if (commit) {
			int total = max - min;
			for (Room r : rooms) {
				r.x -= min;
				r.y -= min;
			}
		}
		StringBuilder raster = new StringBuilder();
		for (int x = min; x <= max; x++) {
			for (int y = min; y <= max; y++) {
				Character c = map.get(new Point(x, y));
				raster.append(c == null ? fill : c);
			}
			raster.append('\n');
		}
		return raster.toString();
	}

	@Override
	public String toString() {
		return rasterize(false);
	}

	public boolean draw(Template t, int x, int y) {
		if (!draw(t, x, y, true)) {
			return false;
		}
		draw(t, x, y, false);
		if (!t.corridor) {
			rooms.add(new Room(x, y, t.width, t.height));
		}
		return true;
	}

	public void set(char c, int x, int y) {
		map.put(new Point(x, y), c);
	}
}
