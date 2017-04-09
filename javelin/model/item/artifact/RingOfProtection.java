package javelin.model.item.artifact;

import javelin.model.unit.Combatant;

/**
 * Adds AC bonus.
 *
 * @author alex
 */
public class RingOfProtection extends Artifact {
	private int bonus;

	/** Constructor. */
	public RingOfProtection(int bonus, int price) {
		super("Ring of protection +" + bonus, price, Slot.FINGER);
		this.bonus = bonus;
	}

	@Override
	protected void apply(Combatant c) {
		c.source.ac += bonus;
	}

	@Override
	protected void negate(Combatant c) {
		c.source.ac -= bonus;
	}

}
