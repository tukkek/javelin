package javelin.model.item.scroll;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.Squad.Transport;
import javelin.model.world.place.Dungeon;
import javelin.model.world.town.Town;

/**
 * See the d20 SRD for more info.
 */
public class RecallScroll extends Scroll {

	private Town town = null;

	public RecallScroll() {
		super("Scroll of word of recall", 1650, Item.MAGIC);
	}

	@Override
	public boolean use(Combatant c) {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		if (Dungeon.active != null) {
			Dungeon.active.leave();
		}
		Squad.active.visual.remove();
		Squad.active.transport = Transport.NONE;
		Squad.active.x = town.x;
		Squad.active.y = town.y;
		Squad.active.displace();
		Squad.active.place();
		return true;
	}

	@Override
	public void produce(Town town) {
		this.town = town;
		name += " (" + town.toString() + ")";
	}
}
