package javelin.controller.generator.dungeon.template.mutator;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import tyrant.mikera.engine.RPG;

public class Wall extends Mutator {
	public Wall() {
		// chance = 1.0;
	}

	@Override
	public void apply(Template t) {
		int nwalls = 1;
		while (RPG.chancein(2)) {
			nwalls += 1;
		}
		for (int i = 0; i < nwalls; i++) {
			if (!generatewall(t)) {
				return;
			}
		}
	}

	boolean generatewall(Template t) {
		ArrayList<Point> spaces = t.find(Template.FLOOR);
		Collections.shuffle(spaces);
		for (Point p : spaces) {
			if (t.countadjacent(Template.FLOOR, p) > 4) {
				carve(t, p);
				return true;
			}
		}
		return false;
	}

	void carve(Template t, Point p) {
		t.tiles[p.x][p.y] = 'X';
		t.tiles[p.x][p.y] = Template.WALL;
		ArrayList<Point> next = new ArrayList<Point>(8);
		for (int x = p.x - 1; x < p.x + 1; x++) {
			for (int y = p.y - 1; y < p.y + 1; y++) {
				Point step = new Point(x, y);
				if (step.validate(0, 0, t.width, t.height)
						&& t.tiles[x][y] == Template.FLOOR
						&& t.countadjacent(Template.WALL, step) <= 1
						&& t.countadjacent('X', step) <= 1) {
					next.add(step);
				}
			}
		}
		if (!next.isEmpty()) {
			carve(t, RPG.pick(next));
		}
	}
}
