package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises {@link Monster#strength} by +2.
 * 
 * @author alex
 */
public class BeltOfGiantStrength extends Artifact {

	private int bonus;

	/** Constructor. */
	public BeltOfGiantStrength(int bonus, int price) {
		super("Belt of giant strength +" + bonus, price, Slot.WAIST);
		this.bonus = bonus;
	}

	@Override
	protected void apply(Combatant c) {
		c.source.raisestrength(bonus / 2);
	}

	@Override
	protected void negate(Combatant c) {
		c.source.raisestrength(-bonus / 2);
	}

}
