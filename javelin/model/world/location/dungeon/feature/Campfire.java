package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.old.RPG;

public class Campfire extends Feature {
	static final String PROMPT = "This room seems safe to rest in. Do you want to set up camp?\n"
			+ "Press c to camp, any other key to cancel...";
	int uses = RPG.r(1, 4);

	public Campfire() {
		super("dungeoncampfire");
		remove = false;
	}

	@Override
	public boolean activate() {
		if (Javelin.prompt(PROMPT) != 'c') {
			return false;
		}
		Lodge.rest(1, 8, true, Lodge.LODGE);
		uses -= 1;
		if (uses == 0) {
			remove();
		}
		return true;
	}
}
