package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.GoodTemple;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * @see GoodTemple
 * @author alex
 */
public class Spirit extends Feature {

	/** Constructor. */
	public Spirit(int xp, int yp) {
		super(xp, yp, "dungeonspirit");
	}

	@Override
	public boolean activate() {
		Feature show = Dungeon.active.getundiscoveredfeature();
		if (show == null) {
			Javelin.message("The spirit flees from your presence in shame...",
					false);
			return true;
		}
		Dungeon.active.discover(show);
		BattleScreen.active.center(show.x, show.y);
		Javelin.redraw();
		String navitext = RPG.chancein(2) ? "'Hey, look!'" : "'Hey, listen!'";
		Javelin.message(navitext, false);
		Point p = JavelinApp.context.getherolocation();
		JavelinApp.context.view(p.x, p.y);
		return true;
	}
}
