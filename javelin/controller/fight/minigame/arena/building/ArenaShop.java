package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Tier;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;

public class ArenaShop extends ArenaBuilding {
	private static final int STOCKSIZE = 9;

	public class ArenaShoppingScreen extends ShoppingScreen {
		protected Combatant buyer;

		public ArenaShoppingScreen(Combatant c) {
			super("What will you buy, " + c + "?", null);
			buyer = c;
		}

		@Override
		protected int getgold() {
			return ArenaFight.get().gold;
		}

		@Override
		protected void spend(Option o) {
			ArenaFight.get().gold -= ((PurchaseOption) o).i.price;
		}

		@Override
		protected List<Combatant> getbuyers() {
			return ArenaFight.get().getgladiators();
		}

		@Override
		protected void afterpurchase(PurchaseOption o) {
			HashMap<Integer, ArrayList<Item>> items = ArenaFight.get().items;
			ArrayList<Item> bag = items.get(buyer.id);
			if (bag == null) {
				bag = new ArrayList<Item>();
				items.put(buyer.id, bag);
			}
			bag.add(o.i);
		}

		@Override
		protected ItemSelection getitems() {
			return stock;
		}

		@Override
		protected void sort(List<Option> options) {
			super.sort(options);
			if (getupgradecost() != null) {
				options.add(new BuildingUpgradeOption());
			}
		}

		@Override
		public boolean select(Option op) {
			BuildingUpgradeOption upgrade = op instanceof BuildingUpgradeOption
					? ((BuildingUpgradeOption) op) : null;
			if (upgrade == null) {
				return super.select(op);
			}
			stayopen = false;
			return upgrade.buy(this);
		}

		@Override
		public String printpriceinfo(Option o) {
			if (o instanceof BuildingUpgradeOption) {
				return " $" + formatcost(o.price);
			}
			return super.printpriceinfo(o);
		}
	}

	public ItemSelection stock = new ItemSelection();

	public ArenaShop() {
		super("Shop", "locationshop",
				"Click this shop to buy items for the active unit!");
		restock();
	}

	void restock() {
		ArrayList<Item> selection = new ArrayList<Item>(
				Tier.ITEMS.get(Tier.values()[level]));
		for (Item i : new ArrayList<Item>(selection)) {
			if (!i.usedinbattle) {
				selection.remove(i);
			}
		}
		Collections.shuffle(selection);
		for (int i = 0; i < STOCKSIZE && i < selection.size(); i++) {
			if (stock.size() <= i) {
				stock.add(null);
			}
			stock.set(i, selection.get(i));
		}
		stock.sort(ItemsByPrice.SINGLETON);
	}

	@Override
	protected boolean click(Combatant current) {
		new ArenaShoppingScreen(current).show();
		return true;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current) + getgoldinfo();
	}

	public static String getgoldinfo() {
		return "\n\nYour gladiators currently have $"
				+ ShoppingScreen.formatcost(ArenaFight.get().gold) + ".";
	}

	@Override
	protected void upgradebuilding() {
		restock();
	}
}
