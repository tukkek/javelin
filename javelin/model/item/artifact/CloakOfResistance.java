package javelin.model.item.artifact;

import javelin.model.unit.Combatant;

/**
 * Adds bonus to saving throws.
 *
 * @author alex
 */
public class CloakOfResistance extends Artifact{

	private int bonus;

	/**
	 * Constructor.
	 */
	public CloakOfResistance(int bonus,int price){
		super("Cloak of resistance +"+bonus,price,Slot.BACK);
		this.bonus=bonus;
	}

	@Override
	protected void apply(Combatant c){
		c.source.fort+=bonus;
		c.source.ref+=bonus;
		c.source.addwill(+bonus);
	}

	@Override
	protected void negate(Combatant c){
		c.source.fort-=bonus;
		c.source.ref-=bonus;
		c.source.addwill(-bonus);
	}
}
