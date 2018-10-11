package javelin.view.screen;

import java.awt.Color;

import javelin.Javelin;
import javelin.old.Screen;

/**
 * Simple notice screen for when a player start the game for the first time.
 *
 * @author alex
 */
public class IntroScreen extends InfoScreen{
	/** Constrcutor. */
	public IntroScreen(final String text){
		super(text);
		configurescreen(this);
		Javelin.app.switchScreen(this);
	}

	/** Configure colors. */
	static public void configurescreen(final Screen s){
		s.setForeground(new Color(192,160,64));
		s.setBackground(new Color(0,0,0));
	}
}