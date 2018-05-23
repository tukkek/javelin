package javelin.old;

import java.awt.event.KeyEvent;

import javelin.Javelin;

public class InputHandler {
	public char getCharacter() {
		return getKeyEvent().getKeyChar();
	}

	public KeyEvent getKeyEvent() {
		throw new Error("Base input handler not overridden");
	}

	public static InputHandler repeat(final char c) {
		return new InputHandler() {
			@Override
			public KeyEvent getKeyEvent() {
				return new KeyEvent(Javelin.app, KeyEvent.KEY_PRESSED,
						System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED,
						c);
			}
		};

	}
}
