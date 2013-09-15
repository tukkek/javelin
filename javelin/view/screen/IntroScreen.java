package javelin.view.screen;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javelin.Javelin;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.InfoScreen;
import tyrant.mikera.tyrant.Screen;

public class IntroScreen extends InfoScreen {
	/**
	 * 
	 */

	public IntroScreen(final String text) {
		super(Javelin.app, text);
		configurescreen(this);
		Javelin.app.switchScreen(this);
	}

	static public void configurescreen(final Screen s) {
		s.setForeground(new Color(192, 160, 64));
		s.setBackground(new Color(0, 0, 0));
	}

	public int numberfeedback() {
		while (true) {
			try {
				return Integer.parseInt(feedback().toString());
			} catch (final NumberFormatException e2) {
				continue;
			}
		}
	}

	static public Character feedback() {
		KeyEvent input = Game.getInput();
		switch (input.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			return '→';
		case KeyEvent.VK_LEFT:
			return '←';
		case KeyEvent.VK_BACK_SPACE:
			return '\b';
		case KeyEvent.VK_SHIFT:
			return feedback();
		}
		return Character.valueOf(input.getKeyChar());
	}
}