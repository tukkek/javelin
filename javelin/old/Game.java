package javelin.old;

import java.awt.event.KeyEvent;

import javelin.controller.db.Preferences;
import javelin.old.messagepanel.MessagePanel;
import javelin.old.messagepanel.TextZone;
import javelin.view.screen.BattleScreen;

public final class Game {
	public static MessagePanel messagepanel;
	// Interface helper object
	public static Interface userinterface = new Interface();
	// Thread for recieveing user input
	public static Thread thread;
	public static boolean delayblock = false;

	// temp: static game instance
	static Game instance = new Game();

	public enum Delay {
		NONE, WAIT, BLOCK
	}

	/**
	 * Main output function for {@link BattleScreen}s.
	 *
	 * @param message
	 *            Text to be printed.
	 * @param t
	 *            TODO remove
	 * @param See
	 *            {@link Delay}.
	 */
	public static void message(final String out, final Delay d) {
		messagepanel.add(out);
		switch (d) {
		case WAIT:
			try {
				redraw();
				Thread.sleep(Preferences.MESSAGEWAIT);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			messagepanel.clear();
			break;
		case BLOCK:
			messagepanel.add("\n" + TextZone.BLACK + "-- ENTER --");
			delayblock = true;
			break;
		}
	}

	public static KeyEvent input() {
		if (messagepanel != null) {
			messagepanel.repaint();
		}
		userinterface.getinput();
		return userinterface.keyevent;
	}

	public static void redraw() {
		BattleScreen.active.mappanel.refresh();
		messagepanel.repaint();
	}
}