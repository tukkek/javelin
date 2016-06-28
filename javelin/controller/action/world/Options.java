package javelin.controller.action.world;

import javelin.view.frame.keys.PreferencesScreen;
import javelin.view.screen.WorldScreen;

/**
 * Opens up a configuration dialog where the user can adjust his preferences.
 * 
 * @author alex
 */
public class Options extends WorldAction {

	/** Constructor. */
	public Options() {
		super("Configure options", new int[] { 'o' }, new String[] { "o" });
	}

	@Override
	public void perform(WorldScreen screen) {
		new PreferencesScreen().show();
	}
}
