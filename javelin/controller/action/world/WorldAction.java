package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.controller.action.Action;
import javelin.controller.action.ActionDescription;
import javelin.controller.action.world.minigame.EnterArena;
import javelin.controller.action.world.minigame.EnterBattlefield;
import javelin.controller.action.world.minigame.EnterZiggurat;
import javelin.view.screen.WorldScreen;

/**
 * An action to be performed by the human player while on the overworld view.
 *
 * @see Action
 * @author alex
 */
public abstract class WorldAction implements ActionDescription {
	/** All world actions. */
	public static final WorldAction[] ACTIONS = new WorldAction[] { //
			new Automate(), // a
			new Camp(), // c
			new Divide(), // d
			new UseItems(), // i
			OpenJournal.getsingleton(), // j
			ShowOptions.getsingleton(), // o
			new Park(), // p
			new Rename(), // r
			new CastSpells(), // s
			new ShowStatistics(), // v
			new EnterArena(), // A
			new EnterBattlefield(), // B
			new ClearHighscore(), // C
			new Infiltrate(), new Dismiss(), // D
			new ZoomWorldIn(), // +
			new ZoomWorldOut(), // +
			new ConfigureWorldKeys(), // K
			new Abandon(), // Q
			new EnterZiggurat(), // Z
			Guide.HOWTO, Guide.MINIGAMES, Guide.ARTIFACTS, Guide.CONDITIONS,
			Guide.ITEMS, Guide.SKILLS, Guide.SPELLS, Guide.UGRADES,
			Guide.DISTRICT, Guide.KITS, Guide.DISCIPLINES,
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD7, }, -1, -1,
					new String[] { "U", "↖ or 7 or U" }),
			new WorldMove(new int[] { KeyEvent.VK_UP, KeyEvent.VK_NUMPAD8 }, 0,
					-1, new String[] { "I", "↑ or 8 or I" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD9 }, 1, -1,
					new String[] { "O", "↗ or 9 or O", }),
			new WorldMove(new int[] { KeyEvent.VK_LEFT, KeyEvent.VK_NUMPAD4 },
					-1, 0, new String[] { "J", "← or 4 or J", }),
			new WorldMove(new int[] { KeyEvent.VK_RIGHT, KeyEvent.VK_NUMPAD6, },
					+1, 0, new String[] { "L", "→ or 6 or L" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD1 }, -1, 1,
					new String[] { "M", "↙ or 1 or M" }),
			new WorldMove(new int[] { KeyEvent.VK_DOWN, KeyEvent.VK_NUMPAD2, },
					0, 1, new String[] { "<", "↓ or 2 or <" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD3 }, 1, 1,
					new String[] { ">", "↘ or 3 or >" }),
			new WorldHelp(), };

	/** Action name. */
	public String name;
	/** {@link Integer} key cods. */
	public final int[] keys;
	/** Text keys. Useful for showing the player. */
	public final String[] morekeys;

	/** Constructor. */
	public WorldAction(final String name, final int[] keysp,
			final String[] morekeysp) {
		this.name = name;
		keys = keysp;
		morekeys = morekeysp;
	}

	/**
	 * Executes action.
	 *
	 * @param screen
	 *            Current screen.
	 */
	abstract public void perform(WorldScreen screen);

	@Override
	public String[] getDescriptiveKeys() {
		return new String[] { morekeys[0] };
	}

	@Override
	public String getDescriptiveName() {
		return name;
	}

	@Override
	public String getMainKey() {
		return morekeys[0];
	}

	@Override
	public void setMainKey(String key) {
		morekeys[0] = key;
	}
}
