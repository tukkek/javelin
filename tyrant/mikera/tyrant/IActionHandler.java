package tyrant.mikera.tyrant;

import javelin.controller.action.Action;
import tyrant.mikera.engine.Thing;


public interface IActionHandler {
    /**
     * Answer true if event propagation should stop, false if it should continue.
     */
    boolean handleAction(Thing actor, Action action, boolean isShiftDown);
}
