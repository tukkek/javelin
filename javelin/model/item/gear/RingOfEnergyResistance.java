package javelin.model.item.gear;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Adds to {@link Monster#energyresistance}.
 *
 * @author alex
 */
public class RingOfEnergyResistance extends Gear{

	private int bonus;

	/** Constructor */
	public RingOfEnergyResistance(int bonus,int price){
		super(getprefix(bonus)+" ring of energy resistance",price,Slot.RING);
		this.bonus=bonus;
	}

	static String getprefix(int bonus){
		if(bonus==2) return "Minor";
		if(bonus==4) return "Major";
		if(bonus==6) return "Greater";
		throw new RuntimeException("Unknown #ringofresistance");
	}

	@Override
	protected void apply(Combatant c){
		c.source.energyresistance+=bonus;
	}

	@Override
	protected void negate(Combatant c){
		c.source.energyresistance-=bonus;
	}

}
