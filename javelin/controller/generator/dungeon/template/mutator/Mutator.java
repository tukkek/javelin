package javelin.controller.generator.dungeon.template.mutator;

import javelin.controller.generator.dungeon.template.Template;

public abstract class Mutator {
	public Double chance = null;

	public abstract void apply(Template t);
}
