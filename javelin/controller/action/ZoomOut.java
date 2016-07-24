package javelin.controller.action;

import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

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
	public boolean perform(Combatant active) {
		BattleScreen.active.mappanel.zoom(-1, true, active.location[0],
				active.location[1]);
		throw new RepeatTurn();
	}

}
