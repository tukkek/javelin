package javelin.view.screen.shopping;

import java.util.ArrayList;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Scroll;
import javelin.model.item.Wand;
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
	/** Constructor. */
	public ShoppingScreen(String s, Town t) {
		super(s, t);
	}

	/**
	 * @param o
	 *            Called after an option has been acquired.
	 */
	protected abstract void afterpurchase(final PurchaseOption o);

	/**
	 * @return Available items.
	 */
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
		Item i = ((PurchaseOption) o).i;
		String useinfo = "";
		if (i instanceof Wand || i instanceof Scroll) {
			ArrayList<Combatant> members = new ArrayList<Combatant>(
					Squad.active.members);
			for (Combatant c : Squad.active.members) {
				if (i.canuse(c) != null) {
					members.remove(c);
				}
			}
			if (members.isEmpty()) {
				useinfo = " - can't use";
			} else {
				useinfo = " - can use: ";
				for (Combatant c : members) {
					useinfo += c + ", ";
				}
				useinfo = useinfo.substring(0, useinfo.length() - 2);
			}
		}
		return " (" + super.printpriceinfo(o).substring(1) + ")" + useinfo;
	}

	static String listactivemembers() {
		int i = 1;
		String s = "";
		for (final Combatant m : Squad.active.members) {
			s += "[" + i++ + "] " + m.toString() + "\n";
		}
		return s;
	}

	@Override
	public String printinfo() {
		return "You have $" + PurchaseScreen.formatcost(Squad.active.gold);
	}
}