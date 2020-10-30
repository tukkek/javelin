package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.feat.attack.GreatCleave;

/**
 * Units that have {@link Cleave} but not {@link GreatCleave} can only cleave
 * once per round.
 *
 * @see Combatant#cleave(float)
 * @author alex
 */
public class Cleaving extends Condition{
	public Cleaving(Combatant c){
		super("cleaving",null,c.ap+1,Effect.NEUTRAL);
	}

	@Override
	public void start(Combatant c){
		//works as a flag
	}

	@Override
	public void end(Combatant c){
		//works as a flag
	}
}
