package tyrant.mikera.tyrant;

import java.awt.event.KeyEvent;

public class InputHandler {
	public char getCharacter() {
		return getKeyEvent().getKeyChar();
	}
	
	public KeyEvent getKeyEvent() {
		throw new Error("Base input handler not overridden");
	}
	
	public static InputHandler repeat(final char c) {
		return new InputHandler() {
			public KeyEvent getKeyEvent() {
				return new KeyEvent(Game.getQuestapp(),
                                    KeyEvent.KEY_PRESSED,
                                    System.currentTimeMillis(),
                                    0,
                                    KeyEvent.VK_UNDEFINED,
                                    c);
			}
		};
		
	}
}

