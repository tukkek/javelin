package javelin.controller.generator.dungeon.template.generated;

import javelin.controller.generator.dungeon.template.Template;

public class Rectangle extends Template {

	@Override
	public void generate() {
		width = 0;
		while (width < 2 || height < 2) {
			initrandom();
		}
	}
}
