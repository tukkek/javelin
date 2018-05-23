package javelin.old;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Stack;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;

public final class Game {
	private static final long serialVersionUID = 3544670698288460592L;

	// temp: static game instance
	private static Game instance = new Game();

	public static transient MessagePanel messagepanel;

	private transient InputHandler inputHandler = null;

	// Interface helper object
	public static Interface userinterface;

	// Thread for recieveing user input
	public static Thread thread;

	public static boolean delayblock = false;

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

	/*
	 * Holds messages in a stack so that they can be displayed later
	 */
	public void pushMessages() {
		messageStack.push(new ArrayList());
	}

	/*
	 * Pulls a stored set of messages off the stack
	 */
	public ArrayList popMessages() {
		return messageStack.pop();
	}

	public void clearMessageList() {
		messageStack = new Stack<>();
	}

	/**
	 * Stack to store messages before they are displayed.
	 *
	 * This is useful so that you can defer the display of messages until after
	 * the result of several actions has been dtermined
	 *
	 * Game.pushMessages(); .... Do something complex here, possibly generating
	 * many messages ArrayList al=Game.popMessages(); Game.message("The
	 * following interesting things happen:); Game.message(al); // now display
	 * the messages
	 *
	 * You can also use this method to suppress messages (use exactly the same
	 * technique, but omit the final line).
	 */
	/**
	 * TODO parametrize
	 */
	public Stack<ArrayList<Object>> messageStack = new Stack<>();
	private boolean isDesigner = false;

	public InputHandler createInputHandler() {
		return new InputHandler() {
			@Override
			public char getCharacter() {
				return getKeyEvent().getKeyChar();
			}

			@Override
			public KeyEvent getKeyEvent() {
				userinterface.getInput();
				return userinterface.keyevent;
			}
		};
	}

	/**
	 * Wait for an single key press
	 */
	public static KeyEvent getInput() {
		return getInput(true);
	}

	public static KeyEvent getInput(final boolean redraw) {
		if (false && redraw && BattleScreen.active.mappanel != null
				&& Javelin.app.isGameScreen()) {
			redraw();
		}
		if (messagepanel != null && messagepanel instanceof MessagePanel) {
			messagepanel.repaint();
		}

		final Game g = Game.instance();
		if (g.inputHandler == null) {
			g.inputHandler = g.createInputHandler();
		}

		return g.inputHandler.getKeyEvent();
	}

	public static void redraw() {
		BattleScreen.active.mappanel.refresh();
		messagepanel.getPanel().repaint();
	}

	/**
	 * Simulate a key press Useful for handling equivalent mouse clicks
	 */
	public static void simulateKey(final char c) {
		if (userinterface != null) {
			final KeyEvent k = new KeyEvent(BattleScreen.active.mappanel, 0,
					System.currentTimeMillis(), 0, 0, 'c');
			k.setKeyChar(c);
			userinterface.go(k);
		}
	}

	public static void infoScreen(final String s) {
		final Screen old = Javelin.app.getScreen();
		final InfoScreen is = new InfoScreen(s);
		Javelin.app.switchScreen(is);
		Game.getInput();
		Javelin.app.switchScreen(old);
	}

	// has same effect as pressing stipulated direction key
	public static void simulateDirection(final int dx, final int dy) {
		switch (dy) {
		case -1:
			switch (dx) {
			case 1:
				simulateKey('9');
				return;
			case 0:
				simulateKey('8');
				return;
			case -1:
				simulateKey('7');
				return;
			}
		case 0:
			switch (dx) {
			case 1:
				simulateKey('6');
				return;
			case 0:
				simulateKey('5');
				return;
			case -1:
				simulateKey('4');
				return;
			}
		case 1:
			switch (dx) {
			case 1:
				simulateKey('3');
				return;
			case 0:
				simulateKey('2');
				return;
			case -1:
				simulateKey('1');
				return;
			}
		}

		return;
	}

	public static char getChar() {
		final KeyEvent k = getInput();
		return k.getKeyChar();
	}

	/**
	 * Temporary access method for Game.instance
	 *
	 * @return
	 */
	public static Game instance() {
		return instance;
	}

	/**
	 * Choose a direction, given as a Point offset
	 */
	public static Point getDirection() {
		while (true) {
			final KeyEvent e = Game.getInput();

			char k = Character.toLowerCase(e.getKeyChar());
			final int i = e.getKeyCode();

			// handle key conversions
			if (i == KeyEvent.VK_UP) {
				k = '8';
			}
			if (i == KeyEvent.VK_DOWN) {
				k = '2';
			}
			if (i == KeyEvent.VK_LEFT) {
				k = '4';
			}
			if (i == KeyEvent.VK_RIGHT) {
				k = '6';
			}
			if (i == KeyEvent.VK_HOME) {
				k = '7';
			}
			if (i == KeyEvent.VK_END) {
				k = '1';
			}
			if (i == KeyEvent.VK_PAGE_UP) {
				k = '9';
			}
			if (i == KeyEvent.VK_PAGE_DOWN) {
				k = '3';
			}
			if (i == KeyEvent.VK_ESCAPE) {
				k = 'q';
			}
			if (k == 'q') {
				return null;
			}
		}
	}

	public void setInputHandler(final InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}

}