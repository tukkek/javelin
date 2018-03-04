package javelin.controller.generator.dungeon.template.corridor;

import javelin.controller.generator.dungeon.template.Template;
import tyrant.mikera.engine.RPG;

/**
 * This is intended to be a "hub" of doors, with the ultimate goal of providing
 * a more "organized" dungeon feel if enough of them are generated. There is a
 * percentage chance that they won't be treated as a {@link Template#corridor}
 * because the hub themselves can be more interesting - enough to provide fights
 * and mutators (like the central room of a dungoen module level).
 *
 * @author alex
 */
public class Hallway extends Template {
	public Hallway() {
		corridor = RPG.chancein(2);
		doors = RPG.r(4, 6);
	}

	@Override
	public void generate() {
		init(RPG.r(4, 10), RPG.r(2, 4));
	}
}
