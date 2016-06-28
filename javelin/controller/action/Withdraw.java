package javelin.controller.action;

import javelin.Javelin;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Thing;

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
	public boolean perform(Combatant active, BattleMap m, Thing thing) {
		Javelin.app.fight.withdraw(active, BattleScreen.active);
		return true;
	}

}
