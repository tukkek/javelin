package javelin.model.unit.condition;

import javelin.model.unit.Combatant;

/**
 * TODO should affect ability checks, better left for when these are enum-based
 * and easier to add a "ability obnus" for ability checks in specific (not
 * ability scores!)
 *
 * @author alex
 */
public class Sickened extends Condition{
	public Sickened(float expireatp,Combatant c,Integer longterm){
		super(c,"sickened",Effect.NEGATIVE,null,expireatp,longterm);
	}

	@Override
	public void start(Combatant c){
		c=c.clone().clonesource();
		Condition.raiseallattacks(c.source,-2,-2);
		Condition.raisesaves(c.source,-2);
		c.skillmodifier-=2;
	}

	@Override
	public void end(Combatant c){
		c=c.clone().clonesource();
		Condition.raiseallattacks(c.source,+2,+2);
		Condition.raisesaves(c.source,+2);
		c.skillmodifier+=2;
	}
}
