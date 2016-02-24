package javelin.model.item;

import java.util.ArrayList;
import java.util.TreeMap;

import javelin.controller.fight.PlanarFight;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.InfoScreen;

/**
 * Keys are found in dungeon treasure chests and allow the player to initiate a
 * {@link PlanarFight}.
 * 
 * @author alex
 */
public class Key extends Item {
	public enum Color {
		BLUE, MAGENTA, WHITE, RED, GREEN, BLACK, TRANSPARENT
	}

	public static TreeMap<Color, String> REALMS = new TreeMap<Color, String>();

	static {
		REALMS.put(Color.MAGENTA, "Magical");
		REALMS.put(Color.BLACK, "Twisted");
		REALMS.put(Color.WHITE, "Holy");
		REALMS.put(Color.RED, "Fire");
		REALMS.put(Color.GREEN, "Earth");
		REALMS.put(Color.TRANSPARENT, "Wind");
		REALMS.put(Color.BLUE, "Water");
	}

	public Color color;

	/**
	 * @see #generate()
	 */
	private Key(Color color) {
		super(color.name().substring(0, 1).toUpperCase()
				+ color.name().substring(1).toLowerCase() + " key", 0, null);
		this.color = color;
	}

	@Override
	public boolean use(Combatant user) {
		return false;
	}

	@Override
	public boolean isusedinbattle() {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		new InfoScreen("").print("Enter a portal to activate this key.");
		Game.getInput();
		return true;
	}

	/**
	 * @param color
	 *            if <code>null</code> generates a key of random color.
	 */
	public static Key generate(Color c) {
		if (c == null) {
			Color[] colors = Color.values();
			c = colors[RPG.r(0, colors.length - 1)];
		}
		return new Key(c);
	}

	public static Key use(Squad active) {
		for (ArrayList<Item> items : active.equipment.values()) {
			for (Item i : items) {
				if (!(i instanceof Key)) {
					continue;
				}
				Game.messagepanel.clear();
				Game.message(
						"Do you want to use your " + i
								+ "? Type y for yes or n for no.",
						null, Delay.NONE);
				Character input = IntroScreen.feedback();
				while (input != 'y' && input != 'n') {
					input = IntroScreen.feedback();
				}
				if (input == 'y') {
					Key k = (Key) i;
					k.unlock();
					return k;
				}
			}
		}
		return null;
	}

	@Override
	public void expend() {
		// isn't used in the traditional manner
	}

	/**
	 * @see #expend()
	 */
	public void unlock() {
		super.expend();
	}
}
