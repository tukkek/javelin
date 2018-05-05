package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.old.Game;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.GoodTemple;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

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
		ArrayList<Feature> features = new ArrayList<Feature>(
				Dungeon.active.features);
		Collections.shuffle(features);
		Feature show = null;
		for (Feature f : features) {
			if (!Dungeon.active.visible[f.x][f.y]) {
				show = f;
				break;
			}
			if (f instanceof Feature && !f.draw) {
				show = f;
				break;
			}
		}
		if (show == null) {
			Javelin.message("The spirit flees from your presence in shame...",
					false);
			return true;
		}
		Dungeon.active.setvisible(show.x, show.y);
		BattleScreen.active.center(show.x, show.y);
		Game.redraw();
		String navitext = RPG.chancein(2) ? "'Hey, look!'" : "'Hey, listen!'";
		Javelin.message(navitext, false);
		if (show instanceof Trap) {
			((Trap) show).discover();
		}
		Point p = JavelinApp.context.getherolocation();
		JavelinApp.context.view(p.x, p.y);
		return true;
	}
}