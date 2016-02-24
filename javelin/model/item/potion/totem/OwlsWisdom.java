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
public class OwlsWisdom extends Potion {

	public OwlsWisdom() {
		super("Potion of owl's wisdom", 300, Item.WIND);
	}

	@Override
	public boolean use(Combatant user) {
		user.source.raisewisdom();
		user.source.raisewisdom();
		user.source.wisdom += 4;
		user.conditions.add(new Buff("wise", user));
		Game.message(
				"Wisdom is now " + Monster.getsignedbonus(user.source.wisdom),
				null, Delay.BLOCK);
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
