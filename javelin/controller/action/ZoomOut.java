package javelin.controller.action;

import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * TODO this should be a view feature in d20
 *
 * @author alex
 */
public class ZoomOut extends Action {

	/** Constructor. */
	public ZoomOut() {
		super("Zoom out", new String[] { "-", "_" });
		allowburrowed = true;
	}

	@Override
	public boolean perform(Combatant active) {
		zoom(active.getlocation());
		throw new RepeatTurn();
	}

	public static void zoom(Point p) {
		BattleScreen.active.mappanel.zoom(-1, true, p.x, p.y);
	}
}
