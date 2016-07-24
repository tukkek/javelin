package javelin.controller.action.world;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * Clear highscore.
 * 
 * @author alex
 */
public class ResetScore extends WorldAction {

	public ResetScore() {
		super(name(), new int[0], new String[] { "R" });
	}

	public static String name() {
		return "Reset your highscore (" + Javelin.gethighscore() + ")";
	}

	@Override
	public void perform(WorldScreen screen) {
		Game.messagepanel.clear();
		Game.message(
				"Are you sure you want to reset your highscore ("
						+ Javelin.gethighscore() + ")? Press y to proceed.\n",
				Delay.NONE);
		boolean ok = InfoScreen.feedback() == 'y';
		if (ok) {
			Javelin.sethighscore(0);
		}
		Game.message((ok ? "Score reset" : "Aborted") + "...", Delay.WAIT);
		name = name();
	}

}
