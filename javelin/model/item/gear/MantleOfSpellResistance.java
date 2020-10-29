package javelin.model.item.gear;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Adds to {@link Monster#energyresistance}.
 *
 * @author alex
 */
public class MantleOfSpellResistance extends Gear{
	int previous;

	/** Constructor */
	public MantleOfSpellResistance(int price){
		super("Mantle of spell resistance",price,Slot.SHOULDERS);
	}

	@Override
	protected void apply(Combatant c){
		c.source.energyresistance+=21;
	}

	@Override
	protected void negate(Combatant c){
		c.source.energyresistance-=21;
	}

}
