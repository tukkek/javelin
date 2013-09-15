package javelin.model.item.cure;

import javelin.model.item.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public abstract class CurePotion extends Potion {

	public CurePotion(final String name, final int price,
			final String description) {
		super("Potion of cure " + name + " wounds", price);
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
