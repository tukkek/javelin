package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Tier;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.shopping.ShoppingScreen;

public class ArenaShop extends ArenaBuilding {
	ItemSelection stock = new ItemSelection();

	public ArenaShop() {
		super("Shop", "locationshop",
				"Click this shop to buy items for the active unit!");
		restock();
	}

	void restock() {
		ArrayList<Item> selection = new ArrayList<Item>(
				Tier.ITEMS.get(Tier.values()[level]));
		Collections.shuffle(selection);
		for (int i = 0; i < 9 && i < selection.size(); i++) {
			if (stock.size() <= i) {
				stock.add(null);
			}
			stock.set(i, selection.get(i));
		}
		stock.sort(ItemsByPrice.SINGLETON);
	}

	@Override
	protected boolean click(Combatant current) {
		return true;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current)
				+ "\n\nYour gladiators currently have $"
				+ ShoppingScreen.formatcost(ArenaFight.get().gold) + ".";
	}
}
