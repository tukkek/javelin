package javelin.model.item.potion;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public class Fly extends Potion {

	public Fly() {
		super("Potion of flight", 750, Item.WIND);
	}

	@Override
	public boolean use(Combatant user) {
		user.source.fly = 60;
		Game.message(user + " floats above the gound!", null, Delay.BLOCK);
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}
