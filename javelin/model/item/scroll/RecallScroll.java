package javelin.model.item.scroll;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.town.Transport;

/**
 * See the d20 SRD for more info.
 */
public class RecallScroll extends Scroll {
	public RecallScroll() {
		super("Scroll of word of recall", 1650, Item.MAGIC, 6,
				SpellsFactor.ratespelllikeability(6));
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		if (Dungeon.active != null) {
			Dungeon.active.leave();
		}
		Squad.active.visual.remove();
		Squad.active.transport = Transport.NONE;
		Squad.active.x = Squad.active.lasttown.x;
		Squad.active.y = Squad.active.lasttown.y;
		Squad.active.displace();
		Squad.active.place();
		return true;
	}
}
