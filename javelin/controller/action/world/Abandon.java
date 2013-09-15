package javelin.controller.action.world;

import javelin.controller.db.StateManager;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public class Abandon extends WorldAction {
	public Abandon() {
		super(
				"Start a completely new game (clears saved data and closes game)",
				new int[] {}, new String[] { "S" });
	}

	@Override
	public void perform(final WorldScreen screen) {
		screen.messagepanel.clear();
		Game.message(
				"Are you sure you want to abandon the current game forever? Press c to continue.",
				null, Delay.NONE);
		if (IntroScreen.feedback() == 'c') {
			StateManager.abandoned = true;
			StateManager.save();
			// StateManager.clearsaves();
			System.exit(0);
		}
	}
}
