package javelin.model.item.potion.totem;

import javelin.model.condition.Buff;
import javelin.model.item.Item;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class EaglesSplendor extends Potion {

	public EaglesSplendor() {
		super("Potion of eagle's splendor", 300, Item.FIRE);
	}

	@Override
	public boolean use(Combatant user) {
		user.source.charisma += 4;
		user.conditions.add(new Buff("fabulous", user));
		Game.message(
				"Charisma is now "
						+ Monster.getsignedbonus(user.source.charisma),
				null, Delay.BLOCK);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
