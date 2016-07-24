package javelin.controller.action;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * Fless from battle at any time if not engaged.
 * 
 * @author alex
 */
public class Withdraw extends Action {

	public Withdraw() {
		super("Withdraw (flee from combat)", "W");
	}

	@Override
	public boolean perform(Combatant active) {
		Javelin.app.fight.withdraw(active, BattleScreen.active);
		return true;
	}

}
