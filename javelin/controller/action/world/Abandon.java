package javelin.controller.action.world;

import javelin.controller.db.StateManager;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * Quit the current game forever, delete the save game so the player can start a
 * new campaign.
 * 
 * @author alex
 */
public class Abandon extends WorldAction {
	/** Constructor. */
	public Abandon() {
		super("Abandon current game", new int[] {}, new String[] { "Q" });
	}

	@Override
	public void perform(final WorldScreen screen) {
		screen.messagepanel.clear();
		Game.message(
				"Are you sure you want to abandon the current game forever? Press c to confirm.",
				Delay.NONE);
		if (InfoScreen.feedback() == 'c') {
			StateManager.abandoned = true;
			StateManager.save(true, StateManager.SAVEFILE);
			System.exit(0);
		}
	}
}
