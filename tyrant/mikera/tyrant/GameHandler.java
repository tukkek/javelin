package tyrant.mikera.tyrant;

import java.awt.event.KeyEvent;

import javelin.controller.action.Action;
import javelin.controller.action.ActionMapping;
import tyrant.mikera.engine.Point;

public class GameHandler {
	private ActionMapping actionMapping;

	public GameHandler() {
		actionMapping = new ActionMapping();
		actionMapping.addDefaultMappings();
		// actionMapping.addRougeLikeMappings();
	}

	public Action actionFor(final KeyEvent keyEvent) {
		return actionMapping.actionFor(keyEvent);
	}

	public Point convertKeyToDirection(final char k) {
		// final ActionDescription action = actionMapping.convertKeyToAction(k);
		// return convertActionToDirection(action);
		return null;
	}

	public void setActionMapping(final ActionMapping actionMapping) {
		this.actionMapping = actionMapping;
	}
}
