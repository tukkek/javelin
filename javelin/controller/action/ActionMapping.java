package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javelin.controller.action.ai.AiMovement;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.action.ai.RangedAttack;
import javelin.controller.exception.RepeatTurnException;

public class ActionMapping {
	private final Map<String, Action> mappings = new HashMap<String, Action>();
	public static Action[] actions = new Action[] { Action.CHARGE, Action.FIRE,
			Action.SPELL, Action.WAIT, UseItem.SINGLETON, GiveItem.SINGLETON,
			Action.LOOK, Action.HELP, Action.ZOOM_OUT, Action.ZOOM_IN,
			Wait.SINGLETON, Action.FLEE, Action.MOVE_N, Action.MOVE_NE,
			Action.MOVE_E, Action.MOVE_SE, Action.MOVE_S, Action.MOVE_SW,
			Action.MOVE_W, Action.MOVE_NW, AiMovement.SINGLETON,
			MeleeAttack.SINGLETON, RangedAttack.SINGLETON, Breath.SINGLETON };

	public ActionDescription convertKeyToAction(final char key) {
		final ActionDescription action = mappings.get(new Character(key));
		return action == null ? Action.UNKNOWN : action;
	}

	public void clear() {
		mappings.clear();
	}

	public void map(final char aChar, final Action action) {
		mappings.put(Character.toString(aChar), action);
	}

	public void addDefaultMappings() {
		addDefaultMovementActions();
		for (final Action a : ActionMapping.actions) {
			for (final String key : a.keys) {
				final Action previous = mappings.put(key, a);
				if (previous != null) {
					throw new RuntimeException("Key " + key + " conflicts ("
							+ a.name + ", " + previous.name + ")");
				}
			}
		}

		// mappings.put("KeyEvent" + KeyEvent.VK_F5, Action.DEBUG);
		// mappings.put(new Character('x'), Action.EXIT);
	}

	public void addDefaultMovementActions() {

	}

	public void addRougeLikeMappings() {
		// Movement
		// mappings.put(new Character('b'), Action.MOVE_SW);
		// mappings.put(new Character('j'), Action.MOVE_S);
		// mappings.put(new Character('n'), Action.MOVE_SE);
		// mappings.put(new Character('h'), Action.MOVE_W);
		// mappings.put(new Character('.'), Action.MOVE_NOWHERE);
		// mappings.put(new Character('l'), Action.MOVE_E);
		// mappings.put(new Character('y'), Action.MOVE_NW);
		// mappings.put(new Character('k'), Action.MOVE_N);
		// mappings.put(new Character('u'), Action.MOVE_NE);
	}

	public Action actionFor(final KeyEvent keyEvent) {
		final char keyChar = keyEvent.getKeyChar();
		if (keyChar == KeyEvent.CHAR_UNDEFINED) {
			final Action action = mappings.get("KeyEvent"
					+ keyEvent.getKeyCode());

			if (action == null) {
				throw new RepeatTurnException();
			}

			return action;
		}
		// Make sure we can control-something events
		if (keyEvent.isControlDown()) {
			// Not sure the puritain JDK will like this
			// Sun should sue those Eclipse guys
			return mappings.get("Control" + keyEvent.getKeyCode());
		}
		final Action action = mappings.get(Character.toString(keyChar));
		if (action == null) {
			System.out.println("Unmapped key " + keyChar);
			throw new RepeatTurnException();
		}
		return action;
	}
}
