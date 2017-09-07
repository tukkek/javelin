package javelin.model.item.artifact;

import javelin.model.unit.attack.Combatant;

/**
 * Turn a unit into a flying unit.
 *
 * @author alex
 */
public class WingsOfFlying extends Artifact {
	int originalfly;
	int originalwalk;

	/** Constructor. */
	public WingsOfFlying(int price) {
		super("Wings of flying", price, Slot.BACK);
	}

	@Override
	protected void apply(Combatant c) {
		originalfly = c.source.fly;
		originalwalk = c.source.walk;
		c.source.fly = 60;
		c.source.walk = 0;
	}

	@Override
	protected void negate(Combatant c) {
		c.source.fly = originalfly;
		c.source.walk = originalwalk;
	}

}
