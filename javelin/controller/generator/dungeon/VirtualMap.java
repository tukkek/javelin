package javelin.controller.generator.dungeon;

import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;

public class VirtualMap {
	public Character fill = '_';
	HashMap<Point, Character> map = new HashMap<Point, Character>();

	public boolean draw(Template t, int rootx, int rooty, boolean check) {
		for (int x = 0; x < t.width; x++) {
			for (int y = 0; y < t.height; y++) {
				Point p = new Point(x + rootx, y + rooty);
				if (!check) {
					map.put(p, t.tiles[x][y]);
					continue;
				}
				Character c = map.get(p);
				if (c != null) {
					return false;
				}
			}
		}
		return true;
	}

	public String rasterize() {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Point p : map.keySet()) {
			min = Math.min(min, Math.min(p.x, p.y));
			max = Math.max(max, Math.max(p.x, p.y));
		}
		StringBuilder raster = new StringBuilder();
		for (int y = max; y >= min; y--) {
			for (int x = min; x <= max; x++) {
				Character c = map.get(new Point(x, y));
				raster.append(c == null ? fill : c);
			}
			raster.append('\n');
		}
		return raster.toString();
	}

	@Override
	public String toString() {
		return rasterize();
	}

	public boolean draw(Template t, int x, int y) {
		if (!draw(t, x, y, true)) {
			return false;
		}
		draw(t, x, y, false);
		return true;
	}
}
