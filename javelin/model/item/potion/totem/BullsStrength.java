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
public class BullsStrength extends Potion {

	public BullsStrength() {
		super("Potion of bull's strength", 300, Item.FIRE);
	}

	@Override
	public boolean use(Combatant user) {
		user.source.raisestrength();
		user.source.raisestrength();
		user.source.strength += 4;
		user.conditions.add(new Buff("strong", user));
		Game.message(
				"Strength is now "
						+ Monster.getsignedbonus(user.source.strength),
				null, Delay.BLOCK);
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
