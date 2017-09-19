package javelin.controller.action;

import javelin.model.unit.attack.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * @see ZoomOut
 * @author alex
 */
public class ZoomIn extends Action {

	/** Constructor. */
	public ZoomIn() {
		super("Zoom in", new String[] { "+", "=" });
		allowburrowed = true;
	}

	@Override
	public boolean perform(Combatant active) {
		BattleScreen.active.mappanel.zoom(+1, true, active.location[0],
				active.location[1]);
		return true;
	}

}
