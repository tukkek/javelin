package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Adds {@link Monster#intelligence}.
 *
 * @author alex
 */
public class HeadbandOfIntellect extends Artifact{

	private int bonus;

	/** Constructor. */
	public HeadbandOfIntellect(int bonus,int price){
		super("Headband of intellect +"+bonus,price,Slot.HEAD);
		this.bonus=bonus;
	}

	@Override
	protected void apply(Combatant c){
		c.source.changeintelligencescore(bonus);
	}

	@Override
	protected void negate(Combatant c){
		c.source.changeintelligencescore(-bonus);
	}

}
