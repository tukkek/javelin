package javelin.model.item.potion;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class Darkvision extends Potion {

	public Darkvision() {
		super("Potion of darkvision", 300, Item.EVIL);
	}

	@Override
	public boolean use(Combatant c) {
		c.source.vision = Monster.VISION_DARK;
		Game.message(c + "'s eyes glow!", null, Delay.BLOCK);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
