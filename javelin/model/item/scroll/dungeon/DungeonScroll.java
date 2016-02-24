package javelin.model.item.scroll.dungeon;

import javelin.model.item.ItemSelection;
import javelin.model.item.scroll.Scroll;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;

/**
 * A scroll that can only be actived in a {@link Dungeon}. Override
 * #usepeacefully() on subclasses.
 * 
 * @author alex
 */
public abstract class DungeonScroll extends Scroll {

	public DungeonScroll(String name, int price, ItemSelection town) {
		super(name, price, town);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean use(Combatant user) {
		return false;
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
