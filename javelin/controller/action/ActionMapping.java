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
import javelin.controller.action.world.Automate;
import javelin.controller.action.world.Guide;
import javelin.controller.action.world.WorldAction;
import javelin.controller.exception.RepeatTurn;

/**
 * Basic listing of in-game {@link Action}s.
 * 
 * @author alex
 * @see WorldAction
 */
public class ActionMapping {
	private final Map<String, Action> mappings = new HashMap<String, Action>();
	/** If true will reload keys. */
	public static boolean reset = false;
	/** Canonical array of possible battle actions. */
	public static Action[] actions = new Action[] { //
			Breath.SINGLETON, // b
			new Charge(), // c
			Dig.SINGLETON, // d
			new Fire(), // f
			new CastSpell(), // s
			UseItem.SINGLETON, // i
			PassItem.SINGLETON, // p
			new TouchAttack(), // t
			Defend.SINGLETON, // w
			new Examine(), // x

			new ActionAdapter(new Automate()), // A
			new DefensiveAttack(), // D
			new Feint(), // F
			new Grapple(), // G
			new ConfigureBattleKeys(), // K
			new Trip(), // T
			new Withdraw(), // W

			new ZoomOut(), // -
			new ZoomIn(), // +

			new ActionAdapter(Guide.HOWTO), new ActionAdapter(Guide.ARTIFACTS),
			new ActionAdapter(Guide.CONDITIONS1),
			new ActionAdapter(Guide.CONDITIONS2),
			new ActionAdapter(Guide.ITEMS), new ActionAdapter(Guide.SKILLS1),
			new ActionAdapter(Guide.SKILLS2), new ActionAdapter(Guide.SPELLS1),
			new ActionAdapter(Guide.SPELLS2), new ActionAdapter(Guide.UGRADES1),
			new ActionAdapter(Guide.UGRADES2), new Help(), // h

			Action.MOVE_N, Action.MOVE_NE, Action.MOVE_E, Action.MOVE_SE,
			Action.MOVE_S, Action.MOVE_SW, Action.MOVE_W, Action.MOVE_NW,
			AiMovement.SINGLETON, MeleeAttack.SINGLETON,
			RangedAttack.SINGLETON };

	public ActionDescription convertKeyToAction(final char key) {
		return mappings.get(new Character(key));
	}

	public void clear() {
		mappings.clear();
	}

	public void map(final char aChar, final Action action) {
		mappings.put(Character.toString(aChar), action);
	}

	public void addDefaultMappings() {
		mappings.clear();
		for (final Action a : ActionMapping.actions) {
			for (final String key : a.keys) {
				if (mappings.put(key, a) != null) {
					throw new RuntimeException("Key conflict (" + key
							+ ") registering action " + a.name);
				}
			}
			for (int code : a.keycodes) {
				if (mappings.put("KeyEvent" + code, a) != null) {
					throw new RuntimeException("Key conflict (" + code
							+ ") registering action " + a.name);
				}
			}
		}
	}

	public Action actionFor(final KeyEvent keyEvent) {
		if (ActionMapping.reset) {
			addDefaultMappings();
		}
		final char keyChar = keyEvent.getKeyChar();
		if (keyChar == KeyEvent.CHAR_UNDEFINED) {
			final Action action =
					mappings.get("KeyEvent" + keyEvent.getKeyCode());

			if (action == null) {
				throw new RepeatTurn();
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
			throw new RepeatTurn();
		}
		return action;
	}
}
