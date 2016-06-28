package javelin.controller.action.world;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.view.screen.StatisticsScreen;
import javelin.view.screen.WorldScreen;

/**
 * Show unit information.
 * 
 * @author alex
 */
public class ShowStatistics extends WorldAction {

	public ShowStatistics() {
		super("View characters", new int[0], new String[] { "v" });
	}

	@Override
	public void perform(WorldScreen screen) {
		int answer = Javelin.choose("Choose a character:",
				WorldScreen.showstatusinformation(), true, false);
		if (answer != -1) {
			new StatisticsScreen(Squad.active.members.get(answer));
		}
	}
}
