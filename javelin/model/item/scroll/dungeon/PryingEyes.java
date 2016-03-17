package javelin.model.item.scroll.dungeon;

import javelin.model.item.Item;
import javelin.model.world.place.Dungeon;

/**
 * Revelas {@link Dungeon} map.
 * 
 * @author alex
 */
public class PryingEyes extends DungeonScroll {

	public PryingEyes() {
		super("Scroll of prying eyes", 1125, Item.MAGIC);
	}

	@Override
	protected boolean usepeacefully() {
		for (int x = 0; x < Dungeon.active.visible.length; x++) {
			for (int y = 0; y < Dungeon.active.visible[x].length; y++) {
				Dungeon.active.visible[x][y] = true;
			}
		}
		return true;
	}

}
