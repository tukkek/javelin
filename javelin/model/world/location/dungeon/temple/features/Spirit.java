package javelin.model.world.location.dungeon.temple.features;

import java.util.ArrayList;
import java.util.Collections;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.model.world.location.dungeon.temple.GoodTemple;

/**
 * @see GoodTemple
 * @author alex
 */
public class Spirit extends Feature {

	/** Constructor. */
	public Spirit(int xp, int yp) {
		super("dog", xp, yp, "dungeonspirit");
	}

	@Override
	public boolean activate() {
		ArrayList<Feature> features =
				new ArrayList<Feature>(Dungeon.active.features);
		Collections.shuffle(features);
		Feature show = null;
		for (Feature f : features) {
			if (!Dungeon.active.visible[f.x][f.y]) {
				show = f;
				break;
			}
			if (f instanceof Trap && !((Trap) f).found) {
				show = f;
				break;
			}
		}
		if (show == null) {
			Javelin.message("The spirit flees from your presence in shame...",
					false);
			return true;
		}
		Javelin.message(
				"This nice spirit tells you the location of something important!",
				false);
		Dungeon.active.visible[show.x][show.y] = true;
		if (show instanceof Trap) {
			((Trap) show).discover();
		}
		JavelinApp.context.view(Dungeon.active.hero);
		return true;
	}
}
