package javelin.model.item.scroll;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.place.Outpost;
import javelin.model.world.place.dungeon.Dungeon;

/**
 * Reveals {@link Dungeon} map or nearby area on the WorldScreen.
 * 
 * @author alex
 */
public class PryingEyes extends Scroll {

	public PryingEyes() {
		super("Scroll of prying eyes", 1125, Item.MAGIC, 5,
				SpellsFactor.ratespelllikeability(5));
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		if (Dungeon.active == null) {
			Outpost.discover(Squad.active.x, Squad.active.y,
					Outpost.VISIONRANGE);
			return true;
		}
		for (int x = 0; x < Dungeon.active.visible.length; x++) {
			for (int y = 0; y < Dungeon.active.visible[x].length; y++) {
				Dungeon.active.visible[x][y] = true;
			}
		}
		return true;
	}

}
