package javelin.model.unit.condition;

import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * @see MeldCrystal
 * @author alex
 */
public class Melding extends Condition{
	/** Constructor. */
	public Melding(Combatant c){
		super("melding",null,Float.MAX_VALUE,Effect.POSITIVE);
	}

	@Override
	public void start(Combatant c){
		c.heal(Math.round(Math.round(Math.ceil(c.maxhp/5f))),true);
		c.source=c.source.clone();
		c.acmodifier+=2;
		for(AttackSequence s:c.source.melee)
			for(Attack a:s)
				a.bonus+=2;
		for(AttackSequence s:c.source.ranged)
			for(Attack a:s)
				a.bonus+=2;
	}

	@Override
	public void end(Combatant c){
		// expires with battle
	}
}
