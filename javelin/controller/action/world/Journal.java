package javelin.controller.action.world;

import javelin.view.frame.keys.TextScreen;
import javelin.view.screen.WorldScreen;

/**
 * Lets a user take notes between sessions.
 * 
 * @author alex
 */
public class Journal extends WorldAction {
	class JournalScreen extends TextScreen {
		JournalScreen() {
			super("Journal");
		}

		@Override
		protected void savetext(String text) {
			content = text;
		}

		@Override
		protected String loadtext() {
			return content;
		}

	}

	/** Journal's content. */
	static public String content =
			"This is your journal, use it to make notes about your current game!";

	/** Constructor. */
	public Journal() {
		super("Journal", new int[] { 'j' }, new String[] { "j" });
	}

	@Override
	public void perform(WorldScreen screen) {
		new JournalScreen().show();
	}
}
