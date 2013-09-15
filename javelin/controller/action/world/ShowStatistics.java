package javelin.controller.action.world;

import java.util.ArrayList;

import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.StatisticsScreen;
import javelin.view.screen.world.WorldScreen;

public class ShowStatistics extends WorldAction {

	public ShowStatistics() {
		super("View character sheet", new int[0], new String[] { "v" });
	}

	@Override
	public void perform(WorldScreen screen) {
		ArrayList<String> names = new ArrayList<String>();
		for (Combatant m : Squad.active.members) {
			names.add(m.toString());
		}
		int answer = CastSpells.choose("Choose a character:", names);
		if (answer != -1) {
			new StatisticsScreen(Squad.active.members.get(answer));
		}
	}
}
