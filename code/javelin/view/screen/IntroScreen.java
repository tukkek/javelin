package javelin.view.screen;

import java.awt.Color;

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
		Screen.change(this);
	}

	/** Configure colors. */
	static public void configurescreen(final Screen s){
		s.setForeground(new Color(192,160,64));
		s.setBackground(new Color(0,0,0));
	}
}