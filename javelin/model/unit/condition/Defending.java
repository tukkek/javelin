package javelin.model.unit.condition;

import javelin.controller.action.Defend;
import javelin.model.unit.Combatant;
import javelin.model.unit.skill.Skill;

/**
 * @see Defend
 *
 * @author alex
 */
public class Defending extends Condition{
	int acbonus=4;

	public Defending(float expireatp,Combatant c){
		super(expireatp,c,Effect.POSITIVE,"defending",null);
		acbonus=Skill.ACROBATICS.getranks(c)>=3?6:4;
	}

	@Override
	public void start(Combatant c){
		c.acmodifier+=acbonus;
	}

	@Override
	public void end(Combatant c){
		c.acmodifier-=acbonus;
	}
}
