package javelin.model.world.location.dungeon.feature.inhabitant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.ItemsByName;
import javelin.controller.comparator.ItemsByPrice;
import javelin.model.item.Item;
import javelin.model.item.precious.PreciousObject;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * Works like a basic {@link Shop} but for {@link Dungeon}s. Has a very small
 * selection of items to sell but adds any items you sell him to his inventory.
 *
 * TODO allow players to attack the trader and get his stuff as reward
 *
 * @author alex
 */
public class Trader extends Inhabitant{
	static final int OPTIONSMIN=3;
	static final int OPTIONSMAX=7;

	List<Item> inventory=new ArrayList<>(7);
	int sell=0;

	/** Reflection-friendly constructor. */
	public Trader(){
		super(Dungeon.active.level+Difficulty.DIFFICULT,
				Dungeon.active.level+Difficulty.DEADLY);
		var cr=Math.max(1,Math.round(inhabitant.source.cr));
		sell=Javelin.round(RewardCalculator.getgold(cr));
		var min=cr;
		var max=cr;
		while(inventory.size()<OPTIONSMIN){
			min-=1;
			max+=1;
			inventory.clear();
			inventory.addAll(stock(min,max));
		}
		sort();
	}

	void sort(){
		Collections.sort(inventory,ItemsByName.SINGLETON);
		Collections.sort(inventory,ItemsByPrice.SINGLETON);
	}

	static List<Item> stock(int mincr,int maxcr){
		if(mincr<1) mincr=1;
		final var mingold=RewardCalculator.getgold(mincr);
		final var maxgold=RewardCalculator.getgold(maxcr);
		var inventory=Item.randomize(Item.ALL).stream()
				.filter(i->mingold<=i.price&&i.price<=maxgold)
				.filter(i->!(i instanceof PreciousObject)).collect(Collectors.toList());
		int inventorysize=RPG.r(OPTIONSMIN,OPTIONSMAX);
		if(inventory.size()<=inventorysize) return inventory;
		Collections.shuffle(inventory);
		while(inventory.size()>inventorysize)
			inventory.remove(0);
		return inventory;
	}

	@Override
	public boolean activate(){
		final var gold=Squad.active.gold;
		var name=inhabitant.toString().toLowerCase();
		var prompt="This "+name+" has a few items to offer.\n\n";
		prompt+="You have $"+Javelin.format(gold)+" gold.";
		var options=new ArrayList<String>(inventory.size());
		for(var item:inventory){
			String detail="$"+Javelin.format(item.price);
			if(!item.canuse(Squad.active.members)) detail+=", can't use";
			options.add(item+" ("+detail+")");
		}
		var sell="Sell items (up to $"+Javelin.format(this.sell)+")";
		options.add(sell);
		var choice=Javelin.choose(prompt,options,true,false);
		if(choice<0) return true;
		if(options.get(choice)==sell){
			sell();
			return true;
		}
		var item=inventory.get(choice);
		if(item.price<=gold){
			Squad.active.gold-=item.price;
			inventory.remove(item);
			item.grab();
		}else{
			Javelin.app.switchScreen(BattleScreen.active);
			Javelin.message("Too expensive...",false);
		}
		return true;
	}

	void sell(){
		var choice=0;
		while(choice>=0){
			var bags=Squad.active.equipment;
			var size=bags.count();
			var items=new ArrayList<Item>(size);
			var options=new ArrayList<String>(size);
			for(var member:Squad.active.members)
				for(var item:bags.get(member)){
					items.add(item);
					var value=Javelin.format(Math.min(item.price,sell));
					options.add("["+member+"] "+item+" ($"+value+")");
				}
			String prompt="Which item to sell?\n\n";
			prompt+=inhabitant+" will pay up to $"+Javelin.format(sell)
					+" for an item.";
			choice=Javelin.choose(prompt,options,true,false);
			if(choice<0) return;
			var sold=items.get(choice);
			bags.remove(sold);
			inventory.add(sold);
			sort();
			Squad.active.gold+=Math.min(sold.price,sell);
		}
	}

	@Override
	public String toString(){
		return "Vendor: "+inventory;
	}
}
