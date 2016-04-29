package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Adds to {@link Monster#resistance}.
 * 
 * @author alex
 */
public class MantleOfSpellResistance extends Artifact {
	int previous;

	/** Constructor */
	public MantleOfSpellResistance(int price) {
		super("Mantle of spell resistance", price, Slot.BACK);
	}

	@Override
	protected void apply(Combatant c) {
		c.source.resistance += 21;
	}

	@Override
	protected void negate(Combatant c) {
		c.source.resistance -= 21;
	}

}
