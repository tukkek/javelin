package javelin.model.item.scroll;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;
import javelin.model.world.Town;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelterScroll extends Item {

	public SecureShelterScroll() {
		super("Scroll of secure shelter", 800, Item.GOOD);
	}

	@Override
	public boolean use(Combatant c) {
		return true;
	}

	@Override
	public boolean isusedinbattle() {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		if (Dungeon.active != null) {
			/* Time cannot pass in dungeon */
			Game.message("Cannot rest at dungeon!", null, Delay.BLOCK);
			return false;
		}
		Town.rest(1, 8);
		return true;
	}

}
