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
public class BearsEndurance extends Potion {

	public BearsEndurance() {
		super("Potion of bear's endurance", 300, Item.EARTH);
	}

	@Override
	public boolean use(Combatant user) {
		user.source.raiseconstitution(user, 2);
		user.conditions.add(new Buff("enduring", user));
		Game.message(
				"Constitution is now "
						+ Monster.getsignedbonus(user.source.constitution),
				null, Delay.BLOCK);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
