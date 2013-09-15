package javelin.controller.action.world;

import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.world.WorldScreen;

public class Rename extends WorldAction {
	public Rename() {
		super("Rename squad members", new int[] {}, new String[] { "r" });
	}

	@Override
	public void perform(WorldScreen screen) {
		for (Combatant m : Squad.active.members) {
			m.source.customName = null;
		}
		/**
		 * TODO This way loses Squad#equipment key. better make Combatant
		 * hashable by id?
		 * */
	}
}
