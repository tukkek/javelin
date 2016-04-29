package javelin.model.item.scroll.dungeon;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.dungeon.Feature;
import javelin.model.world.place.dungeon.Fountain;
import javelin.model.world.place.dungeon.Treasure;
import javelin.view.screen.DungeonScreen;

/**
 * Reveals a {@link Dungeon}'s most powerful feature.
 * 
 * @author alex
 */
public class DiscernLocation extends DungeonScroll {

	public DiscernLocation() {
		super("Scroll of discern location", 3000, Item.MAGIC, 8,
				SpellsFactor.ratespelllikeability(8));
	}

	@Override
	public boolean usepeacefully() {
		Feature f = findkey();
		if (f == null) {
			f = findfountain();
		}
		if (f == null) {
			f = LocateObject.findtreasure();
		}
		if (f == null) {
			DungeonScreen.message("No more features here!");
			return true;
		}
		Dungeon.active.visible[f.x][f.y] = true;
		return true;
	}

	private Feature findfountain() {
		for (Feature f : Dungeon.active.features) {
			if (f instanceof Fountain) {
				return f;
			}
		}
		return null;
	}

	private Feature findkey() {
		for (Feature f : Dungeon.active.features) {
			if (f instanceof Treasure) {
				Treasure t = (Treasure) f;
				if (t.key != null) {
					return f;
				}
			}
		}
		return null;
	}

}
