package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.fight.minigame.arena.ArenaMinigame;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Tier;
import javelin.model.item.gear.Gear;
import javelin.model.unit.Combatant;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;

public class ArenaShop extends ArenaBuilding{
	private static final int STOCKSIZE=9;

	public class ArenaShopScreen extends ShoppingScreen{
		protected Combatant buyer;

		public ArenaShopScreen(Combatant c){
			super("What will you buy, "+c+"?",null);
			buyer=c;
		}

		@Override
		protected int getgold(){
			return ArenaMinigame.get().gold;
		}

		@Override
		protected void spend(Option o){
			ArenaMinigame.get().gold-=((PurchaseOption)o).i.price;
		}

		@Override
		protected List<Combatant> getbuyers(){
			return ArenaMinigame.get().getgladiators();
		}

		@Override
		protected void afterpurchase(PurchaseOption o){
			HashMap<Integer,ArrayList<Item>> items=ArenaMinigame.get().items;
			ArrayList<Item> bag=items.get(buyer.id);
			if(bag==null){
				bag=new ArrayList<>();
				items.put(buyer.id,bag);
			}
			bag.add(o.i);
		}

		@Override
		protected ItemSelection getitems(){
			return stock;
		}
	}

	public ItemSelection stock=new ItemSelection();

	public ArenaShop(){
		super("Shop","locationshop",
				"Click this shop to buy items for the active unit!");
		restock();
	}

	void restock(){
		var selection=Item.BYTIER.get(Tier.TIERS.get(level)).stream()
				.filter(i->i.usedinbattle&&!(i instanceof Gear))
				.collect(Collectors.toList());
		RPG.shuffle(selection);
		var replace=Math.min(stock.size(),selection.size());
		var ascendingprice=stock.sort();
		for(int i=0;i<replace;i++)
			stock.remove(ascendingprice.get(i));
		stock.addAll(selection);
	}

	@Override
	protected boolean click(Combatant current){
		new ArenaShopScreen(current).show();
		return true;
	}

	@Override
	public String getactiondescription(Combatant current){
		return super.getactiondescription(current)+getgoldinfo();
	}

	public static String getgoldinfo(){
		return "\n\nYour gladiators currently have $"
				+Javelin.format(ArenaMinigame.get().gold)+".";
	}

	@Override
	protected void upgradebuilding(){
		restock();
	}
}
