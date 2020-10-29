package javelin.model.item.gear;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Raise {@link Monster#dexterity}
 *
 * @author alex
 */
public class GlovesOfDexterity extends Gear{
	private int bonus;

	/** Constructor. */
	public GlovesOfDexterity(int bonus,int price){
		super("Gloves of dexterity +"+bonus,price,Slot.HANDS);
		this.bonus=bonus/2;
	}

	@Override
	protected void apply(Combatant c){
		c.source.changedexteritymodifier(bonus);
	}

	@Override
	protected void negate(Combatant c){
		c.source.changedexteritymodifier(-bonus);
	}

}
