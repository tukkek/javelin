package javelin.view.frame.keys;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.view.screen.WorldScreen;

/**
 * Preferences screen. Type o in {@link WorldScreen} while in
 * {@link Javelin#DEBUG} mode to {@link #show()}.
 * 
 * @author alex
 */
public class PreferencesScreen extends TextScreen {
	/** Constructor. */
	public PreferencesScreen() {
		super("Options");
	}

	@Override
	protected String loadtext() {
		return Preferences.getfile();
	}

	@Override
	protected void savetext(final String text) {
		Preferences.savefile(text);
	}
}
