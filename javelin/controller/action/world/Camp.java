package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.RandomEncounter;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Accommodations;
import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * Rest in the {@link WorldScreen}. High chance of finding a monster instead.
 * 
 * @author alex
 */
public class Camp extends WorldAction {
	/** Constructor. */
	public Camp() {
		super("Camp", new int[] { KeyEvent.VK_C }, new String[] { "c" });
	}

	@Override
	public void perform(WorldScreen screen) {
		if (Dungeon.active != null) {
			throw new RepeatTurn();
		}
		if (Javelin
				.prompt("Are you sure you want to try to set up camp in this wild area?\n"
						+ "Monsters may be around.\n"
						+ "Press c to camp or any other key to cancel...") != 'c') {
			return;
		}
		for (int i = 0; i < 8; i++) {
			Squad.active.hourselapsed += 1;
			RandomEncounter.encounter(1 / WorldScreen.HOURSPERENCOUNTER);
		}
		Town.rest(1, 0, Accommodations.LODGE);
	}
}
