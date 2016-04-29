package javelin.model.item.scroll.dungeon;

import javelin.model.item.ItemSelection;
import javelin.model.item.scroll.Scroll;
import javelin.model.unit.Combatant;
import javelin.model.world.place.dungeon.Dungeon;

/**
 * A scroll that can only be activated in a {@link Dungeon}. Override
 * #usepeacefully() on subclasses.
 * 
 * @author alex
 */
public abstract class DungeonScroll extends Scroll {

	public DungeonScroll(String name, int price, ItemSelection town, int level,
			float cost) {
		super(name, price, town, level, cost);
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		if (Dungeon.active == null) {
			return false;
		}
		return usepeacefully();
	}

	@Override
	public String describefailure() {
		return "Must be used in a dungeon!";
	}

	abstract protected boolean usepeacefully();

}
