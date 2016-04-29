package javelin.controller.action.world;

import javelin.model.world.Squad;
import javelin.view.screen.StatisticsScreen;
import javelin.view.screen.WorldScreen;

/**
 * Show unit information.
 * 
 * @author alex
 */
public class ShowStatistics extends WorldAction {

	public ShowStatistics() {
		super("View character sheet / show all squad members", new int[0],
				new String[] { "v" });
	}

	@Override
	public void perform(WorldScreen screen) {
		// ArrayList<String> names = new ArrayList<String>();
		// for (Combatant m : Squad.active.members) {
		// names.add(m.toString() + " (" + m.getStatus() + ")");
		// }
		int answer = CastSpells.choose("Choose a character:",
				WorldScreen.showstatusinformation(), true, false);
		if (answer != -1) {
			new StatisticsScreen(Squad.active.members.get(answer));
		}
	}
}
