package javelin.view.screen.shopping;

import java.util.ArrayList;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseOption;
import javelin.view.screen.town.PurchaseScreen;

/**
 * Allows player to buy items.
 * 
 * @author alex
 */
public abstract class ShoppingScreen extends PurchaseScreen {

	public ShoppingScreen(String s, Town t) {
		super(s, t);
	}

	protected abstract void afterpurchase(final PurchaseOption o);

	protected abstract ItemSelection getitems();

	@Override
	public List<Option> getoptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		for (final Item i : getitems()) {
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
		final PurchaseOption o = (PurchaseOption) op;
		Squad.active.gold -= o.i.price;
		afterpurchase(o);
		return true;
	}

	@Override
	public String printpriceinfo(Option o) {
		return " (" + super.printpriceinfo(o).substring(1) + ")";
	}

	public static String listactivemembers() {
		int i = 1;
		String s = "";
		for (final Combatant m : Squad.active.members) {
			s += "[" + i++ + "] " + m.toString() + "\n";
		}
		return s;
	}

}