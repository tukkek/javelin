package javelin.controller.content.action;

import javelin.controller.content.action.ai.Flee;
import javelin.controller.content.fight.Fight;
import javelin.model.unit.Combatant;

/**
 * Fless from battle at any time if not engaged.
 *
 * @see Flee
 * @author alex
 */
public class Withdraw extends Action{

	/** Constructor. */
	public Withdraw(){
		super("Withdraw (flee from combat)","W");
	}

	@Override
	public boolean perform(Combatant active){
		Fight.current.withdraw(active);
		return true;
	}
}
