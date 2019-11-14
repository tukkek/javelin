package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Slot;

/**
 * Raises {@link Monster#wisdom}.
 *
 * @author alex
 */
public class PeriaptOfWisdom extends Artifact{
	private int bonus;

	/** Constructor. */
	public PeriaptOfWisdom(int bonus,int price){
		super("Periapt of wisdom +"+bonus,price,Slot.NECK);
		this.bonus=bonus;
	}

	@Override
	protected void apply(Combatant c){
		c.source.changewisdomscore(bonus);
	}

	@Override
	protected void negate(Combatant c){
		c.source.changewisdomscore(-bonus);
	}

}
