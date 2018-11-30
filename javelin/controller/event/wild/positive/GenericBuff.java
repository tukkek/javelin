package javelin.controller.event.wild.positive;

import javelin.model.unit.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * Buffs or debuffs all attack rolls, saving throws and armor class.
 *
 * @author alex
 */
public abstract class GenericBuff extends Condition{
	int bonus;

	/**
	 * @param bonus Positive bonus for a buff or negative bonus for a debuff.
	 */
	public GenericBuff(Combatant c,int bonus,String description,int longterm){
		super(c,description,bonus>0?Effect.POSITIVE:Effect.NEGATIVE,null,
				Float.MAX_VALUE,longterm);
		this.bonus=bonus;
	}

	@Override
	public void start(Combatant c){
		c.source=c.source.clone();
		raiseallattacks(c.source,bonus,0);
		raisesaves(c.source,bonus);
		c.acmodifier+=bonus;
	}

	@Override
	public void end(Combatant c){
		c.source=c.source.clone();
		raiseallattacks(c.source,-bonus,0);
		raisesaves(c.source,-bonus);
		c.acmodifier-=bonus;
	}
}