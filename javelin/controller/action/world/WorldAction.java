package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.controller.action.ActionDescription;
import javelin.view.screen.WorldScreen;

/**
 * An action to be performed by the human player while on the overworld view.
 * 
 * @author alex
 */
public abstract class WorldAction implements ActionDescription {
	public static final Guide HOWTO =
			new Guide(KeyEvent.VK_F1, "How to play", "F1");
	public static final Guide COMBAT =
			new Guide(KeyEvent.VK_F2, "Combat modifiers", "F2");
	public static final Guide ITEMS = new Guide(KeyEvent.VK_F3, "Items", "F3");
	public static final Guide UGRADES1 =
			new Guide(KeyEvent.VK_F4, "Upgrades 1", "F4");
	public static final Guide UGRADES2 =
			new Guide(KeyEvent.VK_F5, "Upgrades 2", "F5");
	public static final Guide SKILLS =
			new Guide(KeyEvent.VK_F6, "Skills", "F6");
	public static final Guide SPELLS1 =
			new Guide(KeyEvent.VK_F7, "Spells 1", "F7");
	public static final Guide SPELLS2 =
			new Guide(KeyEvent.VK_F8, "Spells 2", "F8");
	public static final Guide ARTIFACTS =
			new Guide(KeyEvent.VK_F9, "Artifacts", "F9");

	String name;
	public final int[] keys;
	public final String[] morekeys;
	public static final WorldAction[] ACTIONS = new WorldAction[] {
			new UseItems(), new CastSpells(), new Divide(), new Rename(),
			new ResetScore(), new ShowStatistics(), new Abandon(),
			new Dismiss(), HOWTO, COMBAT, ITEMS, UGRADES1, UGRADES2, SKILLS,
			SPELLS1, SPELLS2, ARTIFACTS,
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD7, }, -1, -1,
					new String[] { "↖ or 7 or U", "U" }),
			new WorldMove(new int[] { KeyEvent.VK_UP, KeyEvent.VK_NUMPAD8 }, 0,
					-1, new String[] { "↑ or 8 or I", "I" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD9 }, 1, -1,
					new String[] { "↗ or 9 or O", "O" }),
			new WorldMove(new int[] { KeyEvent.VK_LEFT, KeyEvent.VK_NUMPAD4 },
					-1, 0, new String[] { "← or 4 or J", "J" }),
			new WorldMove(new int[] { KeyEvent.VK_RIGHT, KeyEvent.VK_NUMPAD6, },
					+1, 0, new String[] { "→ or 6 or L", "L" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD1 }, -1, 1,
					new String[] { "↙ or 1 or M", "M" }),
			new WorldMove(new int[] { KeyEvent.VK_DOWN, KeyEvent.VK_NUMPAD2, },
					0, 1, new String[] { "↓ or 2 or <", "<" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD3 }, 1, 1,
					new String[] { "↘ or 3 or >", ">" }),
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

}
