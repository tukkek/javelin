package javelin.controller.action;

import javelin.controller.exception.RepeatTurn;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Thing;

/**
 * TODO this should be a view feature in d20
 * 
 * @author alex
 */
public class ZoomOut extends Action {

	public ZoomOut() {
		super("Zoom out", new String[] { "-", "_" });
		allowwhileburrowed = true;
	}

	@Override
	public boolean perform(Combatant active, BattleMap m, Thing thing) {
		BattleScreen.active.mappanel.zoom(-1, true, thing.x, thing.y);
		throw new RepeatTurn();
	}

}
