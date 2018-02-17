package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;

public class Rotate extends Mutator {
	public static final Rotate INSTANCE = new Rotate();

	private Rotate() {
		chance = .5;
	}

	@Override
	public void apply(Template t) {
		char[][] rotated = new char[t.height][t.width];
		for (int x = 0; x < t.width; x++) {
			for (int y = 0; y < t.height; y++) {
				rotated[y][x] = t.tiles[x][y];
			}
		}
		t.tiles = rotated;
		Point dimensions = new Point(t.width, t.height);
		t.width = dimensions.y;
		t.height = dimensions.x;
	}
}
