package javelin.model.world.location.dungeon.temple.features;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.StairsUp;
import javelin.model.world.location.dungeon.temple.MagicTemple;
import javelin.view.screen.DungeonScreen;

/**
 * @see MagicTemple
 * @author alex
 */
public class Portal extends Feature {
	/** Constructor. */
	public Portal(int xp, int yp) {
		super("dog", xp, yp, "locationportal");
		remove = false;
	}

	@Override
	public boolean activate() {
		if (Javelin.prompt(
				"Do you want to enter the portal?\nPress enter to cross it, any other key to cancel...") != '\n') {
			return true;
		}
		StairsUp stairs = null;
		for (Feature f : Dungeon.active.features) {
			if (f instanceof StairsUp) {
				stairs = (StairsUp) f;
				break;
			}
		}
		Dungeon.active.herolocation = new Point(stairs.x - 1, stairs.y);
		DungeonScreen.updatelocation = false;
		return true;
	}
}
