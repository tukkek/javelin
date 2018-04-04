package javelin.model.world.location.town.labor.productive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Potion;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.order.CraftingOrder;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;

public class Shop extends Location {
	public static class BuildShop extends Build {
		public BuildShop() {
			super("Build shop", 5, null, Rank.HAMLET);
		}

		@Override
		protected void define() {
			super.define();
			cost = Math.min(cost, Item.getselection(town.originalrealm).size());
		}

		@Override
		public Location getgoal() {
			return new Shop(false, town.originalrealm);
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d) && d.getlocationtype(Shop.class).isEmpty();
		}
	}

	class ShowShop extends ShoppingScreen {
		Shop s;

		ShowShop(Shop s) {
			super("You enter the shop.", null);
			this.s = s;
		}

		@Override
		protected ItemSelection getitems() {
			return selection;
		}

		@Override
		protected void afterpurchase(PurchaseOption o) {
			s.crafting.add(new CraftingOrder(o.i, crafting));
		}

		@Override
		public String printinfo() {
			return super.printinfo() + (crafting.queue.isEmpty() ? ""
					: "\n\nCurrently crafting: " + crafting);
		}
	}

	class UpgradeShop extends BuildingUpgrade {
		public UpgradeShop(Shop s, int newlevel) {
			super("", newlevel - s.level, newlevel, s, Rank.HAMLET);
			name = "Upgrade shop";
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public boolean validate(District d) {
			return cost > 0 && crafting.queue.isEmpty() && super.validate(d);
		}

		@Override
		public void done() {
			super.done();
			level = upgradelevel;
			stock();
		}
	}

	ItemSelection selection = new ItemSelection();
	OrderQueue crafting = new OrderQueue();
	int level = 0;
	Realm selectiontype;

	public Shop(boolean first, Realm r) {
		super(r.prefixate() + " shop");
		allowentry = false;
		discard = false;
		gossip = true;
		level = 5;
		selectiontype = World.scenario.randomrealms ? Realm.random() : r;
		if (first) {
			selection.add(new Potion(new CureLightWounds()));
		}
		stock();
	}

	void stock() {
		ItemSelection items = getselection();
		if (items.size() > 20 && level > 10) {
			items = new ItemSelection(items);
			Collections.shuffle(items);
		}
		for (Item i : items) {
			if (selection.size() >= level) {
				break;
			}
			selection.add(i.clone());
		}
	}

	@Override
	public Integer getel(int attackerel) {
		return Integer.MIN_VALUE;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		for (Order o : crafting.reclaim(Squad.active.hourselapsed)) {
			CraftingOrder done = (CraftingOrder) o;
			done.item.grab();
		}
		new ShowShop(this).show();
		return true;
	}

	@Override
	public boolean hascrafted() {
		return crafting.reportanydone();
	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		int newlevel = level + 5;
		newlevel = Math.min(newlevel, d.town.getrank().maxpopulation);
		newlevel = Math.min(newlevel, getselection().size());
		newlevel = Math.min(newlevel, 20);
		ArrayList<Labor> upgrades = super.getupgrades(d);
		upgrades.add(new UpgradeShop(this, newlevel));
		return upgrades;
	}

	ItemSelection getselection() {
		return Item.getselection(selectiontype);
	}

	@Override
	public boolean isworking() {
		return !crafting.queue.isEmpty() && !crafting.reportalldone();
	}

	@Override
	public boolean canupgrade() {
		return super.canupgrade() && crafting.isempty();
	}
}
