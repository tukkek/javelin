package javelin.controller.generator.dungeon.template.mutator;

import java.util.Arrays;

import javelin.controller.generator.dungeon.template.Template;

public class HorizontalMirror extends Mutator {
	public static final HorizontalMirror INSTANCE = new HorizontalMirror();

	private HorizontalMirror() {
		chance = .5;
	}

	@Override
	public void apply(Template t) {
		char[][] original = Arrays.copyOf(t.tiles, t.width);
		for (int x = 0; x < t.width; x++) {
			t.tiles[t.width - x - 1] = original[x];
		}
	}
}
