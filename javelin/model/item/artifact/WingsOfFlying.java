package javelin.model.item.artifact;

import javelin.model.unit.Combatant;

/**
 * Turn a unit into a flying unit.
 * 
 * @author alex
 */
public class WingsOfFlying extends Artifact {
	private int without;

	/** Constructor. */
	public WingsOfFlying(int price) {
		super("Wings of flying", price, Slot.BACK);
	}

	@Override
	protected void apply(Combatant c) {
		without = c.source.fly;
		c.source.fly = 60;
	}

	@Override
	protected void negate(Combatant c) {
		c.source.fly = without;
	}

}
