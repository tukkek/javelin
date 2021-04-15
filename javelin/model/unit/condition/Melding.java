package javelin.model.unit.condition;

import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;

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
		c.acmodifier+=2;
		for(var a:c.source.getattacks())
			a.bonus+=2;
	}

	@Override
	public void end(Combatant c){
		// expires with battle
	}
}
