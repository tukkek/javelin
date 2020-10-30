package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * A fatigued character can neither run nor charge and takes a -2 penalty to
 * Strength and Dexterity. Spells that use this must understand that generally a
 * fatigued creature becomes exhausted if it receives another level of fatigue.
 *
 * @author alex
 */
public class Fatigued extends Condition{
	/** Full constructor for subclasses. */
	protected Fatigued(Integer spelllevel,Integer casterlevel,float expireatp,
			Integer longtermp,Effect effectp){
		super("fatigued",spelllevel,casterlevel,expireatp,longtermp,effectp);
	}

	protected Fatigued(String descriptionp,Spell s,Integer hours){
		super(descriptionp,s,Float.MAX_VALUE,hours,Effect.NEGATIVE);
	}

	public Fatigued(Spell s,Integer hours){
		this("fatigued",s,hours);
	}

	@Override
	public void start(Combatant c){
		c.source=c.source.clone();
		c.source.changestrengthmodifier(-1);
		c.source.changeconstitutionmodifier(c,-1);
	}

	@Override
	public void end(Combatant c){
		c.source.changestrengthmodifier(+1);
		c.source.changeconstitutionmodifier(c,+1);
	}

}
