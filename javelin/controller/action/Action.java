package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * One of the game moves, to be used during battle.
 * 
 * @see AiAction
 * @see ActionMapping
 * @author alex
 */
public abstract class Action implements Serializable, ActionDescription {
	private static final long serialVersionUID = 1L;

	// Movement
	public static final Movement MOVE_NW = new Movement("Move (↖)",
			new String[] { "7", "KeyEvent" + KeyEvent.VK_HOME, "U" },
			"↖ (7 on numpad, or U)");
	public static final Movement MOVE_N = new Movement("Move (↑)",
			new String[] { "8", "KeyEvent" + KeyEvent.VK_UP, "I" },
			"↑ (8 on numpad, or I)");
	public static final Movement MOVE_NE = new Movement("Move (↗)",
			new String[] { "9", "KeyEvent" + KeyEvent.VK_PAGE_UP, "O" },
			"↗ (9 on numpad, or O)");
	public static final Movement MOVE_W = new Movement("Move (←)",
			new String[] { "4", "KeyEvent" + KeyEvent.VK_LEFT, "J", "←" },
			"← (4 on numpad, or J)");
	public static final Movement MOVE_E = new Movement("Move (→)",
			new String[] { "6", "KeyEvent" + KeyEvent.VK_RIGHT, "L", "→" },
			"→ (6 on numpad, or L)");
	public static final Movement MOVE_SW = new Movement("Move (↙)",
			new String[] { "1", "KeyEvent" + KeyEvent.VK_END, "M" },
			"↙ (1 in numpad, or M)");
	public static final Movement MOVE_S = new Movement("Move (↓)",
			new String[] { "2", "KeyEvent" + KeyEvent.VK_DOWN, "<" },
			"↓ (2 in numpad, or <)");
	public static final Movement MOVE_SE = new Movement("Move (↘)",
			new String[] { "3", "KeyEvent" + KeyEvent.VK_PAGE_DOWN, ">" },
			"↘ (3 on numpad, or >)");

	public final String name;

	public String[] keys = new String[0];
	public int[] keycodes = new int[0];
	public boolean allowwhileburrowed = false;

	public Action(final String name, final String key) {
		this(name, new String[] { key });
	}

	public Action(final String name, final String[] keys) {
		this(name);
		this.keys = keys;
	}

	public Action(final String name2) {
		name = name2;
	}

	public Action(String namep, int[] keys) {
		name = namep;
		keycodes = keys;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isMovementKey() {
		return this == Action.MOVE_E || this == Action.MOVE_N
				|| this == Action.MOVE_NE || this == Action.MOVE_NW
				|| this == Action.MOVE_S || this == Action.MOVE_SE
				|| this == Action.MOVE_SW || this == Action.MOVE_W;
	}

	static protected TreeMap<Integer, Float> distributeRoll(final int dices,
			final int sides) {
		final int nCombinations = dices * sides;
		final int[][] combinations = new int[nCombinations][dices];
		for (int dice = 0; dice < dices; dice++) {
			for (int i = 0, value = 1, valueUse = 0; i < nCombinations; i++) {
				combinations[i][dice] = value;
				if (++valueUse == dice + 1) {
					valueUse = 0;
					value = value == sides ? 1 : value + 1;
				}
			}
		}
		final TreeMap<Integer, Integer> results =
				new TreeMap<Integer, Integer>();
		for (final int[] combination : combinations) {
			int sum = 0;
			for (final int element : combination) {
				sum += element;
			}
			final Integer ocurrences = results.get(sum);
			results.put(sum, (ocurrences == null ? 0 : ocurrences) + 1);
		}

		final TreeMap<Integer, Float> distribution =
				new TreeMap<Integer, Float>();
		for (final Entry<Integer, Integer> result : results.entrySet()) {
			distribution.put(result.getKey(),
					result.getValue() / (float) nCombinations);
		}
		return distribution;
	}

	/**
	 * @param list
	 *            The possible outcomes of the action decided by the AI.
	 * @param b
	 * @return The actual outcome of the action made by the AI.
	 */
	public static Node outcome(final List<ChanceNode> list,
			boolean enableoverrun) {
		float roll = RPG.random();
		for (final ChanceNode cn : list) {
			roll -= cn.chance;
			if (roll <= 0) {
				BattleScreen.active.updatescreen(cn, enableoverrun);
				return cn.n;
			}
		}
		for (final ChanceNode cn : list) {
			System.err.println("Outcome error! " + cn.action + " " + cn.chance);
		}
		throw new RuntimeException("Couldn't determine outcome: " + roll);
	}

	@Override
	public String[] getDescriptiveKeys() {
		return keys;
	}

	@Override
	public String getDescriptiveName() {
		return name;
	}

	public boolean isPressed(final Character key) {
		for (final String k : keys) {
			if (k.equals(key.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Performs an action as the human player.
	 * 
	 * @param active
	 *            Current unit.
	 * @param m
	 *            Current map.
	 * @param thing
	 *            Unit's visual representation.
	 * @return <code>true</code> if executed an action (successfully or not).
	 */
	public abstract boolean perform(Combatant active, javelin.model.BattleMap m,
			Thing thing);

	public static Node outcome(List<ChanceNode> list) {
		return outcome(list, false);
	}

	public static float bind(float misschance) {
		if (misschance > .95f) {
			return .95f;
		}
		if (misschance < .05f) {
			return .05f;
		}
		return misschance;
	}

	@Override
	public String getMainKey() {
		return keys.length == 0 ? null : keys[0];
	}

	@Override
	public void setMainKey(String key) {
		if (keys.length > 0) {
			keys[0] = key;
		}
	}
}
