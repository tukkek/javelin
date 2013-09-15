package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.option.Option;

/**
 * Scrolls only out of combat.
 * 
 * @author alex
 */
public class ShoppingScreen extends PurchaseScreen {

	// private static final Option REEQUIP = new Option("Reorganize equipment",
	// 0);

	public class PurchaseOption extends Option {
		private final Item i;

		public PurchaseOption(final Item i) {
			super(i.name, i.price);
			this.i = i;
		}

	}

	public ShoppingScreen(final Town town) {
		super("Buy", town);
	}

	/* TODO items takes 1day/1000gp to make (min:1) */
	@Override
	List<Option> getOptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		for (final Item i : Item.all) {
			list.add(new PurchaseOption(i));
		}
		return list;
	}

	@Override
	boolean select(final Option op) {
		if (op.price > Squad.active.gold) {
			text += "Not enough $!\n";
			return false;
		}
		final String originaltext = text;
		String s = "\n";
		s += listactivemembers();
		final PurchaseOption o = (PurchaseOption) op;
		text += "\n"
				+ o.i.name
				// + ": "
				// + o.i.description
				+ s
				+ "\nWhich squad member will carry it? Press r to cancel purchase.";
		Combatant m = null;
		while (m == null) {
			Javelin.app.switchScreen(this);
			try {
				final Character input = IntroScreen.feedback();
				if (input == 'r') {
					text = originaltext;
					return false;
				}
				if (input == PROCEED) {
					return true;
				}
				m = Squad.active.members
						.get(Integer.parseInt(input.toString()) - 1);
			} catch (final NumberFormatException e) {
				continue;
			} catch (final IndexOutOfBoundsException e) {
				continue;
			}
		}
		Squad.active.equipment.get(m.toString()).add((Item) o.i.clone());
		Squad.active.gold -= o.i.price;
		new ShoppingScreen(town).show();
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
}
