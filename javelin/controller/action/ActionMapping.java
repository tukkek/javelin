package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javelin.controller.action.ai.AiMovement;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.action.ai.RangedAttack;
import javelin.controller.action.maneuver.DefensiveAttack;
import javelin.controller.action.maneuver.Feint;
import javelin.controller.action.maneuver.Grapple;
import javelin.controller.action.maneuver.Trip;
import javelin.controller.action.world.Guide;
import javelin.controller.exception.RepeatTurnException;

/**
 * Basic listing of in-game {@link Action}s.
 * 
 * @author alex
 */
public class ActionMapping {
	private final Map<String, Action> mappings = new HashMap<String, Action>();
	public static Action[] actions = new Action[] { //
			Breath.SINGLETON, // b
			Action.CHARGE, // c
			new DefensiveAttack(), // d
			new Feint(), // F
			Action.FIRE, // f
			new Grapple(), // G
			Action.SPELL, // s
			Action.WAIT, // w
			UseItem.SINGLETON, // i
			PassItem.SINGLETON, // p
			new TouchAttack(), new Trip(), // T
			Wait.SINGLETON, // w
			Action.WITHDRAW, // W

			Action.LOOK, // x
			Action.ZOOM_OUT, // -
			Action.ZOOM_IN, // +

			new ActionAdapter(Guide.HOWTO), // F1
			new ActionAdapter(Guide.COMBAT), // F2
			new ActionAdapter(Guide.ITEMS), // F3
			new ActionAdapter(Guide.UGRADES1), // F4
			new ActionAdapter(Guide.UGRADES2), // F5
			new ActionAdapter(Guide.SPELLS1), // F6
			new ActionAdapter(Guide.SPELLS2), // F7
			Action.HELP, // h

			Action.MOVE_N, Action.MOVE_NE, Action.MOVE_E, Action.MOVE_SE,
			Action.MOVE_S, Action.MOVE_SW, Action.MOVE_W, Action.MOVE_NW,
			AiMovement.SINGLETON, MeleeAttack.SINGLETON,
			RangedAttack.SINGLETON };

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
		for (final Action a : ActionMapping.actions) {
			boolean overwrite = false;
			for (final String key : a.keys) {
				if (mappings.put(key, a) != null) {
					overwrite = true;
				}
			}
			for (int code : a.keycodes) {
				if (mappings.put("KeyEvent" + code, a) != null) {
					overwrite = true;
				}
			}
			if (overwrite) {
				throw new RuntimeException(
						"Key conflict registering action " + a.name);
			}
		}
	}

	public Action actionFor(final KeyEvent keyEvent) {
		final char keyChar = keyEvent.getKeyChar();
		if (keyChar == KeyEvent.CHAR_UNDEFINED) {
			final Action action =
					mappings.get("KeyEvent" + keyEvent.getKeyCode());

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
			throw new RepeatTurnException();
		}
		return action;
	}
}
