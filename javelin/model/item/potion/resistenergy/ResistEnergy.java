package javelin.model.item.potion.resistenergy;

import javelin.model.condition.Buff;
import javelin.model.item.Item;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public abstract class ResistEnergy extends Potion {
	protected int resistance;

	public ResistEnergy(int resistancep, int price) {
		super("Potion of resist energy " + resistancep, price, Item.MAGIC);
		resistance = resistancep;
	}

	@Override
	public boolean use(Combatant c) {
		c.source.resistance += resistance;
		c.conditions.add(new Buff("resists energy", c));
		return true;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		return false;
	}

}