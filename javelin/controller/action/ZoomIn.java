package javelin.controller.action;

import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Thing;

/**
 * @see ZoomOut
 * @author alex
 */
public class ZoomIn extends Action {

	public ZoomIn() {
		super("Zoom in", new String[] { "+", "=" });
		allowwhileburrowed = true;
	}

	@Override
	public boolean perform(Combatant active, BattleMap m, Thing thing) {
		BattleScreen.active.mappanel.zoom(+1, true, thing.x, thing.y);
		return true;
	}

}
