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
		Feature show = findtarget();
		if (show == null) {
			Javelin.message("The spirit flees from your presence in shame...",
					false);
			return true;
		}
		discover(show);
		BattleScreen.active.center(show.x, show.y);
		Game.redraw();
		String navitext = RPG.chancein(2) ? "'Hey, look!'" : "'Hey, listen!'";
		Javelin.message(navitext, false);
		Point p = JavelinApp.context.getherolocation();
		JavelinApp.context.view(p.x, p.y);
		return true;
	}

	public static void discover(Feature f) {
		Dungeon.active.setvisible(f.x, f.y);
		f.discover(null, 9000);
	}

	public static Feature findtarget() {
		ArrayList<Feature> features = Dungeon.active.features.copy();
		Collections.shuffle(features);
		for (Feature f : features) {
			if (!Dungeon.active.visible[f.x][f.y] || !f.draw) {
				return f;
			}
		}
		return null;
	}
}
