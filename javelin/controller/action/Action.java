package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javelin.controller.Roller;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

public class Action implements Serializable, ActionDescription {
	private static final long serialVersionUID = 1L;

	private static int nextId = 0;

	// Movement
	public static final ActionDescription UNKNOWN = new Action("Unknown",
			new String[] {});
	public static final Movement MOVE_NW = new DiagonalMovement(
			"Move up and left", new String[] { "7",
					"KeyEvent" + KeyEvent.VK_HOME, "U" },
			"↖ (7 on numpad, or U)");
	public static final Movement MOVE_N = new Movement("Move up", new String[] {
			"8", "KeyEvent" + KeyEvent.VK_UP, "I" }, "↑ (8 on numpad, or I)");
	public static final Movement MOVE_NE = new DiagonalMovement(
			"Move up and right", new String[] { "9",
					"KeyEvent" + KeyEvent.VK_PAGE_UP, "O" },
			"↗ (9 on numpad, or O)");
	public static final Movement MOVE_W = new Movement("Move left",
			new String[] { "4", "KeyEvent" + KeyEvent.VK_LEFT, "J", "←" },
			"← (4 on numpad, or J)");
	public static final Movement MOVE_E = new Movement("Move right",
			new String[] { "6", "KeyEvent" + KeyEvent.VK_RIGHT, "L", "→" },
			"→ (6 on numpad, or L)");
	public static final DiagonalMovement MOVE_SW = new DiagonalMovement(
			"Move down and left", new String[] { "1",
					"KeyEvent" + KeyEvent.VK_END, "M" },
			"↙ (1 in numpad, or M)");
	public static final Movement MOVE_S = new Movement("Move down",
			new String[] { "2", "KeyEvent" + KeyEvent.VK_DOWN, "<" },
			"↓ (2 in numpad, or <)");
	public static final Movement MOVE_SE = new DiagonalMovement(
			"Move down and right", new String[] { "3",
					"KeyEvent" + KeyEvent.VK_PAGE_DOWN, ">" },
			"↘ (3 on numpad, or >)");

	// Actions
	public static final ActionDescription APPLY_SKILL = new Action(
			"apply skill", "r");
	public static final ActionDescription CHAT = new Action("chat", "c");
	public static final ActionDescription DROP = new Action("drop", "d");
	public static final ActionDescription EAT = new Action("eat", "e");
	public static final Fire FIRE = new Fire(
			"Fire or throw ranged weapon (-4 penalty for shooting at an opponent who is engaged in close combat)",
			"f", 'f');
	public static final ActionDescription GIVE = new Action("give", "g");
	public static final ActionDescription INVENTORY = new Action("inventory",
			"i");
	public static final ActionDescription JUMP = new Action("jump", "j");
	public static final ActionDescription KICK = new Action("kick", "k");
	public static final Action LOOK = new Action("look", new String[] { "l",
			"x" });
	/* TODO messages */
	public static final Action MESSAGES = new Action("messages", "m");
	public static final ActionDescription OPEN = new Action("open", "o");
	public static final ActionDescription PICKUP = new Action("pickup", "p");
	public static final ActionDescription QUAFF = new Action("quaff", "q");
	public static final ActionDescription READ = new Action("read", "r");
	public static final ActionDescription SEARCH = new Action("search", "s");
	public static final ActionDescription THROW = new Action("throw", "t");
	public static final ActionDescription USE = new Action("use", "u");
	public static final ActionDescription VIEW_STATS = new Action("view_stats",
			"v");
	public static final ActionDescription WIELD = new Action("wield", "w");
	public static final Action WAIT = new Action(
			"wait (+4 armor class until next turn)", new String[] { "w", ".",
					"5" });
	public static final ActionDescription EXIT = new Action("exit", "<");
	public static final ActionDescription PRAY = new Action("pray", "_");
	public static final ActionDescription PRAY2 = new Action("pray2", "y");
	public static final ActionDescription ZAP = new Action("zap", "z");
	public static final ActionDescription ZAP_AGAIN = new Action("zap_again",
			"`");
	public static final Action HELP = new Action("help", new String[] { "?",
			"h" });
	public static final ActionDescription SELECT_TILE = new Action(
			"select_tile", ";");
	public static final Action SAVE_GAME = new Action("save", "-");
	public static final Action LOAD_GAME = new Action("load", "+");
	public static final ActionDescription QUIT_GAME = new Action("quit", "q");
	public static final ActionDescription SHOW_QUESTS = new Action(
			"show_quests", "#");
	public static final Action ZOOM_OUT = new Action("zoom", "(");
	public static final Action ZOOM_IN = new Action("zoom", ")");

	public static final ActionDescription OK = new Action("ok", "KeyEvent"
			+ KeyEvent.VK_ENTER);
	public static final ActionDescription CANCEL = new Action("cancel",
			"KeyEvent" + KeyEvent.VK_ESCAPE);

	public static final ActionDescription DROP_EXTENDED = new Action(
			"drop_extended", 'D');
	public static final ActionDescription PICKUP_EXTENDED = new Action(
			"pickup_extended", 'P');

	public static final Action FLEE = new Action("Flee from combat", "F");
	public static final Action CHARGE = new Charge("Charge", "c");

	public static final Action SPELL = new CastSpell("Cast spells", "s");

	public final String name;
	private final int id;

	private static Map allById;

	public String[] keys = new String[0];

	public Action(final String name, final String key) {
		this(name, new String[] { key });
	}

	public Action(final String name, final int id) {
		this.name = name;
		this.id = id;
		getAllById().put(new Integer(id), this);
	}

	public Action(final String name, final String[] keys) {
		this(name);
		this.keys = keys;
	}

	public Action(final String name2) {
		this(name2, nextId++);
	}

	private Map getAllById() {
		if (allById == null) {
			allById = new HashMap();
		}
		return allById;
	}

	@Override
	public String toString() {
		return name;
	}

	public Object readResolve() {
		return allById.get(new Integer(id));
	}

	public boolean isMovementKey() {
		return this == Action.MOVE_E || this == Action.MOVE_N
				|| this == Action.MOVE_NE || this == Action.MOVE_NW
				|| this == Action.MOVE_S || this == Action.MOVE_SE
				|| this == Action.MOVE_SW || this == Action.MOVE_W;
	}

	public List<List<ChanceNode>> getSucessors(final BattleState gameState,
			final Combatant combatant) {
		return Collections.emptyList();
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
		final TreeMap<Integer, Integer> results = new TreeMap<Integer, Integer>();
		for (final int[] combination : combinations) {
			int sum = 0;
			for (final int element : combination) {
				sum += element;
			}
			final Integer ocurrences = results.get(sum);
			results.put(sum, (ocurrences == null ? 0 : ocurrences) + 1);
		}

		final TreeMap<Integer, Float> distribution = new TreeMap<Integer, Float>();
		for (final Entry<Integer, Integer> result : results.entrySet()) {
			distribution.put(result.getKey(), result.getValue()
					/ (float) nCombinations);
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
		float roll = Roller.RANDOM.nextFloat();
		for (final ChanceNode cn : list) {
			roll -= cn.chance;
			if (roll <= 0) {
				BattleScreen.active.updatescreen(cn, enableoverrun);
				return cn.n;
			}
		}
		for (final ChanceNode cn : list) {
			System.out.println("Outcome error! " + cn.action + " " + cn.chance);
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

	public boolean perform(Combatant hero, javelin.model.BattleMap m) {
		return false;
	}

	public static Node outcome(List<ChanceNode> list) {
		return outcome(list, false);
	}

}
