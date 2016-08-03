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
import javelin.controller.action.world.OpenJournal;
import javelin.controller.action.world.ShowOptions;
import javelin.controller.action.world.WorldAction;
import javelin.controller.exception.RepeatTurn;

/**
 * Basic listing of in-game {@link Action}s.
 * 
 * @author alex
 * @see WorldAction
 */
public class ActionMapping {
	/** Only instance of this class. */
	public static final ActionMapping SINGLETON = new ActionMapping();
	/** If true will reload keys. */
	public static boolean reset = false;
	/** Canonical array of possible battle actions. */
	public static Action[] actions = new Action[] { //
			Breath.SINGLETON, // b
			new Charge(), // c
			Dig.SINGLETON, // d
			Fire.SINGLETON, // f
			new CastSpell(), // s
			UseItem.SINGLETON, // i
			new ActionAdapter(OpenJournal.getsingleton()), // o
			new ActionAdapter(ShowOptions.getsingleton()), // o
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
			new ActionAdapter(Guide.CONDITIONS), new ActionAdapter(Guide.ITEMS),
			new ActionAdapter(Guide.SKILLS), new ActionAdapter(Guide.SPELLS),
			new ActionAdapter(Guide.UGRADES), //

			new Help(), // h

			Action.MOVE_N, Action.MOVE_NE, Action.MOVE_E, Action.MOVE_SE,
			Action.MOVE_S, Action.MOVE_SW, Action.MOVE_W, Action.MOVE_NW,
			AiMovement.SINGLETON, MeleeAttack.SINGLETON,
			RangedAttack.SINGLETON };

	final Map<String, Action> mappings = new HashMap<String, Action>();

	private ActionMapping() {
		init();
	}

	/**
	 * Register {@link Action#keys} and {@link Action#keycodes}.
	 */
	public void init() {
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

	/**
	 * @return The appropriate acttion for the given {@link KeyEvent}.
	 */
	public Action getaction(final KeyEvent keyEvent) {
		if (ActionMapping.reset) {
			mappings.clear();
			init();
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
