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
		Town t = (Town) Squad.active.findnearest(Town.class);
		if (t != null && t.getdistrict().getarea().contains(Squad.active.getlocation())) {
			Javelin.message("Cannot camp inside a town's district.\nTry moving further into the wilderness.\n", false);
			return;
		}
		Character input = Javelin.prompt(
				"Are you sure you want to try to set up camp in this wild area?\n" + "Monsters may interrupt you.\n\n"
						+ "Press c to camp for a day, w to camp for a week or any other key to cancel...");
		int hours;
		int restat;
		if (input == 'c') {
			hours = 8;
			restat = 8;
		} else if (input == 'w') {
			hours = 24 * 7;
			restat = 12;
		} else {
			return;
		}
		for (int i = 0; i < hours; i++) {
			Squad.active.hourselapsed += 1;
			RandomEncounter.encounter(1 / WorldScreen.HOURSPERENCOUNTER);
			if (i > 0 && (i + 1) % restat == 0) {
				Town.rest(1, 0, Accommodations.LODGE);
				// System.out.println("rest");
			}
		}
	}
}
