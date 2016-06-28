package javelin.controller.action;

import javelin.controller.exception.RepeatTurn;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Thing;

/**
 * Lets the player examine the surroundings and monsters.
 * 
 * @author alex
 */
public class Examine extends Action {

	public Examine() {
		super("examine", new String[] { "x" });
		allowwhileburrowed = true;
	}

	@Override
	public boolean perform(Combatant active, BattleMap m, Thing thing) {
		BattleScreen.active.doLook();
		throw new RepeatTurn();
	}
}
