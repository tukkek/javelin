package javelin.model.item.gear;

import javelin.model.unit.Combatant;
import javelin.model.unit.Slot;

/**
 * Adds AC bonus.
 *
 * @author alex
 */
public class RingOfProtection extends Gear{
	private int bonus;

	/** Constructor. */
	public RingOfProtection(int bonus,int price){
		super("Ring of protection +"+bonus,price,Slot.FINGERS);
		this.bonus=bonus;
	}

	@Override
	protected void apply(Combatant c){
		c.source.setrawac(c.source.getrawac()+bonus);
	}

	@Override
	protected void negate(Combatant c){
		c.source.setrawac(c.source.getrawac()-bonus);
	}
}
