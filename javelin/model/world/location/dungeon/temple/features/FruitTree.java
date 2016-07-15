package javelin.model.world.location.dungeon.temple.features;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.temple.EarthTemple;

/**
 * @see EarthTemple
 * @author alex
 */
public class FruitTree extends Feature {
	/** Constructor. */
	public FruitTree(int xp, int yp) {
		super("dog", xp, yp, "dungeontreefruit");
	}

	@Override
	public boolean activate() {
		if (Javelin.prompt("Take a fruit from the tree?\n"
				+ "Press ENTER to confirm, any other key to cancel...") != '\n') {
			return false;
		}
		Combatant target = Squad.active.members.get(Javelin.choose(
				"Who will eat the fruit?", Squad.active.members, true, true));
		target.hp = target.maxhp;
		return true;
	}

}
