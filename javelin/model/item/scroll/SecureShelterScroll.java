package javelin.model.item.scroll;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.town.Town;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelterScroll extends Scroll {

	public SecureShelterScroll() {
		super("Scroll of secure shelter", 800, Item.GOOD, 4,
				SpellsFactor.ratespelllikeability(4));
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
