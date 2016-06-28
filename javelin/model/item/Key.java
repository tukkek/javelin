package javelin.model.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javelin.controller.fight.PlanarFight;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Portal;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Keys are found in dungeon treasure chests and allow the player to initiate a
 * {@link PlanarFight}.
 * 
 * It's good practice to open a {@link Portal} whenever a Key is taken so the
 * player can have someplace, somewhere in the map to use it on.
 * 
 * TODO instead of using keys for portals would be better to have 7 Realm
 * Temples (el = 3 6 9 12 15 18 21). Each temple needs a key to be unlocked or
 * can be forced (Str take 10 to bash or Disable Device check) widh DC relative
 * to EL. Temples are permanent, 4-level-deep {@link Dungeon}s with 1 ruby per
 * level in a chest and thematic battles (like current {@link PlanarFight}s). In
 * the deepest level there is also 1 Emblem of Realm, which you need all 7 of
 * the win the game by going back to the Temple of Haxor. Keys in
 * {@link Dungeon}s are only generated based on the Temples that haven't yet
 * been unlocked.
 * 
 * @see Portal#opensafe()
 * @author alex
 */
public class Key extends Item {
	public static LinkedList<Realm> queue = new LinkedList<Realm>();

	static {
		regeneratequeue();
	}

	/** Color/realm of this key. */
	public Realm color;

	/**
	 * @see #generate()
	 */
	private Key(Realm color) {
		super(color.toString() + " key", 0, null);
		this.color = color;
		usedinbattle = false;
	}

	private static void regeneratequeue() {
		for (Realm r : Realm.values()) {
			queue.add(r);
		}
		Collections.shuffle(queue);
		/*
		 * TODO
		 * 
		 * in the future would be better to ignore all realms that have already
		 * been opened and keys in player's possessions
		 * 
		 * this means that at some point it's possible #generate will return
		 * null
		 */
	}

	@Override
	public boolean use(Combatant user) {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		new InfoScreen("").print("Enter a portal to activate this key.");
		Game.getInput();
		return true;
	}

	/**
	 * @param realm
	 *            if <code>null</code> generates a key of random color.
	 */
	public static Key generate() {
		if (queue.isEmpty()) {
			regeneratequeue();
		}
		return new Key(queue.pop());
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
