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
		super(c,"cleaving",Effect.NEUTRAL,null,c.ap+1);
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
