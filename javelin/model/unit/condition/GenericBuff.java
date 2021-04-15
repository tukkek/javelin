package javelin.model.unit.condition;

import javelin.model.unit.Combatant;

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
		super(description,null,null,Float.MAX_VALUE,longterm,
				bonus>0?Effect.POSITIVE:Effect.NEGATIVE);
		this.bonus=bonus;
	}

	@Override
	public void start(Combatant c){
		raiseallattacks(c.source,bonus,0);
		raisesaves(c.source,bonus);
		c.acmodifier+=bonus;
	}

	@Override
	public void end(Combatant c){
		raiseallattacks(c.source,-bonus,0);
		raisesaves(c.source,-bonus);
		c.acmodifier-=bonus;
	}
}