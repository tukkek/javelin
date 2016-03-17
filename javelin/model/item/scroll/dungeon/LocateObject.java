package javelin.model.item.scroll.dungeon;

import javelin.controller.walker.Walker;
import javelin.model.dungeon.Feature;
import javelin.model.dungeon.Treasure;
import javelin.model.item.Item;
import javelin.model.world.place.Dungeon;
import javelin.view.screen.world.DungeonScreen;

/**
 * Allows player to find nearest treasure chest in a {@link Dungeon}.
 */
public class LocateObject extends DungeonScroll {
	public LocateObject() {
		super("Scroll of locate object", 150, Item.MAGIC);
	}

	@Override
	public boolean usepeacefully() {
		Feature closest = findtreasure();
		if (closest == null) {
			DungeonScreen.message("No treasure left.");
			return true;
		}
		Dungeon.active.visible[closest.x][closest.y] = true;
		return true;
	}

	public static Feature findtreasure() {
		Treasure closest = null;
		for (Feature f : Dungeon.active.features) {
			if (f instanceof Treasure) {
				Treasure t = (Treasure) f;
				if (closest == null) {
					closest = t;
				} else if (Walker.distance(Dungeon.active.hero.x,
						Dungeon.active.hero.y, t.x,
						t.y) < Walker.distance(Dungeon.active.hero.x,
								Dungeon.active.hero.y, closest.x, closest.y)) {
				}

			}
		}
		return closest;
	}
}
