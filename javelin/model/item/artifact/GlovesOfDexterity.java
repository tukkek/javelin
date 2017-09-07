package javelin.model.item.artifact;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * Raise {@link Monster#dexterity}
 *
 * @author alex
 */
public class GlovesOfDexterity extends Artifact {
	private int bonus;

	/** Constructor. */
	public GlovesOfDexterity(int bonus, int price) {
		super("Gloves of dexterity +" + bonus, price, Slot.HAND);
		this.bonus = bonus / 2;
	}

	@Override
	protected void apply(Combatant c) {
		c.source.raisedexterity(bonus);
	}

	@Override
	protected void negate(Combatant c) {
		c.source.raisedexterity(-bonus);
	}

}
