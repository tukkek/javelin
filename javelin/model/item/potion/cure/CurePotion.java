package javelin.model.item.potion.cure;

import javelin.model.item.ItemSelection;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * See the d20 SRD for more info.
 */
public abstract class CurePotion extends Potion {

	public CurePotion(final String name, final int price, final ItemSelection town) {
		super("Potion of cure " + name + " wounds", price, town);
	}

	abstract int rollhpcured();

	@Override
	public boolean use(final Combatant c) {
		final Monster m = c.source;
		usepeacefully(c);
		Game.message(m + " is now " + c.getStatus(), null, Delay.BLOCK);
		return true;
	}

	@Override
	public boolean usepeacefully(final Combatant m) {
		m.hp += rollhpcured();
		if (m.hp > m.maxhp) {
			m.hp = m.maxhp;
		}
		return true;
	}
}
