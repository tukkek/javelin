package javelin.controller.action;

import javelin.Javelin;
import javelin.controller.action.ai.Flee;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * Fless from battle at any time if not engaged.
 * 
 * @see Flee
 * @author alex
 */
public class Withdraw extends Action {

	/** Constructor. */
	public Withdraw() {
		super("Withdraw (flee from combat)", "W");
	}

	@Override
	public boolean perform(Combatant active) {
		final Fight f = Javelin.app.fight;
		if (!f.canflee) {
			Game.message("Cannot flee!", Delay.BLOCK);
			BattleScreen.active.block();
			throw new RepeatTurn();
		}
		f.withdraw(active, BattleScreen.active);
		return true;
	}
}
