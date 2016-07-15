package javelin.view.screen.shopping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.Artificer;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseOption;
import javelin.view.screen.town.PurchaseScreen;

/**
 * See {@link Artificer}.
 * 
 * @author alex
 */
public class ArtificerScreen extends ShoppingScreen {
	static final Option SELL = new Option("Sell item", 0, 's');
	final Artificer artificer;

	/**
	 * @param artificer
	 * @param s
	 * @param t
	 */
	public ArtificerScreen(Artificer artificer) {
		super("You enter the artificer's laboratory. \"Should I forge something for you, milord?\"",
				null);
		this.artificer = artificer;
		stayopen = false;
	}

	@Override
	protected ItemSelection getitems() {
		return this.artificer.selection;
	}

	@Override
	protected Comparator<Option> sort() {
		final Comparator<Option> normal = super.sort();
		return new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) {
				if (o1 == SELL) {
					return +1;
				}
				if (o2 == SELL) {
					return -1;
				}
				return normal.compare(o1, o2);
			}
		};
	}

	@Override
	protected void afterpurchase(PurchaseOption o) {
		artificer.craft(o);
	}

	@Override
	public List<Option> getoptions() {
		List<Option> options = super.getoptions();
		options.add(SELL);
		return options;
	}

	@Override
	public boolean select(Option op) {
		if (op == SELL) {
			ArrayList<Combatant> squad =
					new ArrayList<Combatant>(Squad.active.members);
			for (Combatant c : Squad.active.members) {
				if (getbag(c).isEmpty()) {
					squad.remove(c);
				}
			}
			if (squad.isEmpty()) {
				return false;
			}
			int selleri = Javelin.choose("Who will sell an item?", squad, true,
					false);
			if (selleri < 0) {
				return false;
			}
			Combatant seller = squad.get(selleri);
			ArrayList<Item> bag = new ArrayList<Item>(getbag(seller));
			for (Item i : new ArrayList<Item>(bag)) {
				if (i.price == 0) {
					bag.remove(i);
				}
			}
			ArrayList<String> sellingprices = new ArrayList<String>(bag.size());
			for (Item i : bag) {
				sellingprices.add(i + " ($"
						+ PurchaseScreen.formatcost(i.price / 2) + ")");
			}
			int bagi = Javelin.choose("Sell which item?", sellingprices, true,
					false);
			if (bagi >= 0) {
				sell(seller, bag.get(bagi));
				/* hack so the gold will update TODO */
				show();
				return true;
			}
			return false;
		}
		return super.select(op);
	}

	ArrayList<Item> getbag(Combatant seller) {
		return Squad.active.equipment.get(seller.id);
	}

	void sell(Combatant seller, Item sold) {
		Artifact a = sold instanceof Artifact ? (Artifact) sold : null;
		if (a != null && seller.equipped.contains(a)) {
			a.remove(seller);
		}
		getbag(seller).remove(sold);
		Squad.active.gold += sold.price / 2;
	}

	@Override
	public String printpriceinfo(Option o) {
		return o == SELL ? "" : super.printpriceinfo(o);
	}
}