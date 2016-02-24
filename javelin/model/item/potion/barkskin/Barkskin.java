package javelin.model.item.potion.barkskin;

import javelin.model.condition.Buff;
import javelin.model.item.Item;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public abstract class Barkskin extends Potion {
	public Barkskin(int bonus, int price) {
		super("Potion of barkskin +" + bonus, price, Item.EARTH);
	}

	@Override
	public boolean use(Combatant c) {
		c.source.ac += 3;
		c.conditions.add(new Buff("barkskin", c));
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}