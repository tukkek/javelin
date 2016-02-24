package javelin.controller.action.world;

import javelin.Javelin;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.InfoScreen;

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
				"Are you sure you want to reset your highscore? Press y to proceed.\n",
				null, Delay.NONE);
		boolean ok = InfoScreen.feedback() == 'y';
		if (ok) {
			Javelin.sethighscore(0);
		}
		Game.message((ok ? "Score reset" : "Aborted") + "...", null,
				Delay.WAIT);
		name = name();
	}

}
