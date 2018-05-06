package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises {@link Monster#charisma}.
 *
 * @author alex
 */
public class CloakOfCharisma extends Artifact {

	private int bonus;

	/** Constructor. */
	public CloakOfCharisma(int bonus, int price) {
		super("Cloak of charisma +" + bonus, price, Slot.BACK);
		this.bonus = bonus;
	}

	@Override
	protected void apply(Combatant c) {
		c.source.changecharismamodifier(bonus);
	}

	@Override
	protected void negate(Combatant c) {
		c.source.changecharismamodifier(-bonus);
	}

}
