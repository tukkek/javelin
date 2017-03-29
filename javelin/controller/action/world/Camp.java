package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.RandomEncounter;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Lodge;
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
		Town t = (Town) Squad.active.getnearest(Town.class);
		if (t != null && t.getdistrict().getarea()
				.contains(Squad.active.getlocation())) {
			Javelin.message(
					"Cannot camp inside a town's district!\nTry moving further into the wilderness.\n",
					false);
			return;
		}
		String prompt = "Are you sure you want to try to set up camp in this wild area?\n"
				+ "Monsters may interrupt you.\n\n"
				+ "Press c to set camp, w to camp for a week or any other key to cancel...";
		if (Javelin.DEBUG) {
			prompt = "DEBUG CAMP\n"
					+ "(c)amp (d)ay (w)eek (m)onth (s)eason (y)ear?";
		}
		Character input = Javelin.prompt(prompt);
		int[] period = pickperiod(input);
		if (period == null) {
			return;
		}
		final int hours = period[0];
		final int restat = period[1];
		for (int i = 0; i < hours; i++) {
			Squad.active.hourselapsed += 1;
			RandomEncounter.encounter(1 / WorldScreen.HOURSPERENCOUNTER);
			if (i > 0 && (i + 1) % restat == 0) {
				Lodge.rest(1, 0, Lodge.LODGE);
			}
		}
	}

	int[] pickperiod(Character input) {
		if (input == 'c') {
			return new int[] { 8, 2 };
		}
		if (input == 'd' && Javelin.DEBUG) {
			return new int[] { 24, 12 };
		}
		if (input == 'w') {
			return new int[] { 24 * 7, 12 };
		}
		if (input == 'm' && Javelin.DEBUG) {
			return new int[] { 24 * 30, 12 };
		}
		if (input == 's' && Javelin.DEBUG) {
			return new int[] { 24 * 100, 12 };
		}
		if (input == 'y' && Javelin.DEBUG) {
			return new int[] { 24 * 400, 12 };
		}
		return null;
	}
}
