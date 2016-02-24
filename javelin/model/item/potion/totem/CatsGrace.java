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
public class CatsGrace extends Potion {

	public CatsGrace() {
		super("Potion of cat's grace", 300, Item.WATER);
	}

	@Override
	public boolean use(Combatant user) {
		user.source.raisedexterity(2);
		user.source.dexterity += 4;
		user.conditions.add(new Buff("graceful", user));
		Game.message(
				"Dexterity is now "
						+ Monster.getsignedbonus(user.source.dexterity),
				null, Delay.BLOCK);
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
