package tyrant.mikera.tyrant;

import javelin.controller.action.Action;

public interface IActionHandler {
	/**
	 * Answer true if event propagation should stop, false if it should
	 * continue.
	 */
	boolean handleAction(Action action, boolean isShiftDown);
}
