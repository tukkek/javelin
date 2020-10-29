package javelin.model.item.gear;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Raises {@link Monster#charisma}.
 *
 * @author alex
 */
public class CloakOfCharisma extends Gear{

	private int bonus;

	/** Constructor. */
	public CloakOfCharisma(int bonus,int price){
		super("Cloak of charisma +"+bonus,price,Slot.SHOULDERS);
		this.bonus=bonus;
	}

	@Override
	protected void apply(Combatant c){
		c.source.changecharismamodifier(bonus);
	}

	@Override
	protected void negate(Combatant c){
		c.source.changecharismamodifier(-bonus);
	}

}
