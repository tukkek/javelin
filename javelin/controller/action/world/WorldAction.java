package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.controller.action.Action;
import javelin.controller.action.ActionDescription;
import javelin.view.screen.WorldScreen;

/**
 * An action to be performed by the human player while on the overworld view.
 * 
 * @see Action
 * @author alex
 */
public abstract class WorldAction implements ActionDescription {
	public static final Guide HOWTO =
			new Guide(KeyEvent.VK_F1, "How to play", "F1");
	public static final Guide ARTIFACTS =
			new Guide(KeyEvent.VK_F2, "Artifacts", "F2");
	public static final Guide CONDITIONS1 =
			new Guide(KeyEvent.VK_F3, "Conditions 1", "F3");
	public static final Guide CONDITIONS2 =
			new Guide(KeyEvent.VK_F4, "Conditions 2", "F4");
	public static final Guide ITEMS = new Guide(KeyEvent.VK_F5, "Items", "F5");
	public static final Guide SKILLS1 =
			new Guide(KeyEvent.VK_F6, "Skills 1", "F6");
	public static final Guide SKILLS2 =
			new Guide(KeyEvent.VK_F7, "Skills 2", "F7");
	public static final Guide SPELLS1 =
			new Guide(KeyEvent.VK_F8, "Spells 1", "F8");
	public static final Guide SPELLS2 =
			new Guide(KeyEvent.VK_F9, "Spells 2", "F9");
	public static final Guide UGRADES1 =
			new Guide(KeyEvent.VK_F10, "Upgrades 1", "F10");
	public static final Guide UGRADES2 =
			new Guide(KeyEvent.VK_F11, "Upgrades 2", "F11");

	String name;
	public final int[] keys;
	public final String[] morekeys;
	public static final WorldAction[] ACTIONS = new WorldAction[] {
			new Divide(), // d
			new UseItems(), // i
			new Journal(), // i
			new Options(), // o
			new Park(), // p
			new Rename(), // r
			new CastSpells(), // s
			new ShowStatistics(), // v
			new Work(), // v
			new Automate(), // A
			new Dismiss(), // D
			new ConfigureWorldKeys(), // K
			new Abandon(), // Q
			new ResetScore(), // R
			HOWTO, ARTIFACTS, CONDITIONS1, CONDITIONS2, ITEMS, SKILLS1, SKILLS2,
			SPELLS1, SPELLS2, UGRADES1, UGRADES2,
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

	public WorldAction(final String name, final int[] is,
			final String[] morekeysp) {
		this.name = name;
		keys = is;
		morekeys = morekeysp;
	}

	@Deprecated
	public WorldAction(final String name2, final int[] is) {
		this(name2, is, new String[] {});
	}

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
