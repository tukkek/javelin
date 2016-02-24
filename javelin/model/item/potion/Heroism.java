package javelin.model.item.potion;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class Heroism extends Potion {

	public Heroism() {
		super("Potion of heroism", 750, Item.FIRE);
	}

	@Override
	public boolean use(Combatant user) {
		Game.message(javelin.model.spell.Heroism.makeheroic(user), null,
				Delay.BLOCK);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
