package javelin.view.screen.town;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.potion.Potion;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.Option;

/**
 * Screen in which the player buys items.
 * 
 * TODO items takes 1day/1000gp to make (min:1, potion=1)
 * 
 * @author alex
 */
public class ShoppingScreen extends PurchaseScreen {
	public class PurchaseOption extends Option {
		private final Item i;

		public PurchaseOption(final Item i) {
			super(i.name, i.price);
			this.i = i;
		}

	}

	public ShoppingScreen(final Town town) {
		super("Buy:", town);
	}

	@Override
	public List<Option> getOptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		for (final Item i : town.items) {
			list.add(new PurchaseOption(i));
		}
		return list;
	}

	@Override
	public boolean select(final Option op) {
		if (op.price > Squad.active.gold) {
			text += "Not enough $!\n";
			return false;
		}
		// final String originaltext = text;
		// String s = "\n";
		// s += listactivemembers();
		final PurchaseOption o = (PurchaseOption) op;
		Squad.active.gold -= o.i.price;
		town.crafting.add(new Serializable[] { (Item) o.i.clone() },
				o.i instanceof Potion ? 24
						: Math.max(24, 24 * Math.round(o.i.price / 1000f)));
		return true;
	}

	static public String listactivemembers() {
		int i = 1;
		String s = "";
		for (final Combatant m : Squad.active.members) {
			s += "[" + i++ + "] " + m.toString() + "\n";
		}
		return s;
	}

	@Override
	public String printpriceinfo(Option o) {
		return " (" + super.printpriceinfo(o).substring(1) + ")";
	}
}
