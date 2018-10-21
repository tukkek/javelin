package javelin.controller.fight.minigame.arena.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.fight.minigame.arena.Arena;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Tier;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
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
			return Arena.get().gold;
		}

		@Override
		protected void spend(Option o){
			Arena.get().gold-=((PurchaseOption)o).i.price;
		}

		@Override
		protected List<Combatant> getbuyers(){
			return Arena.get().getgladiators();
		}

		@Override
		protected void afterpurchase(PurchaseOption o){
			HashMap<Integer,ArrayList<Item>> items=Arena.get().items;
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
		ArrayList<Item> selection=new ArrayList<>(
				Tier.ITEMS.get(Tier.values()[level]));
		for(Item i:new ArrayList<>(selection))
			if(!i.usedinbattle||i instanceof Artifact) selection.remove(i);
		Collections.shuffle(selection);
		for(int i=0;i<STOCKSIZE&&i<selection.size();i++){
			if(stock.size()<=i) stock.add(null);
			stock.set(i,selection.get(i));
		}
		stock.sort(ItemsByPrice.SINGLETON);
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
				+Javelin.format(Arena.get().gold)+".";
	}

	@Override
	protected void upgradebuilding(){
		restock();
	}
}
