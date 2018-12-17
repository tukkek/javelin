package javelin.model.world.location.town.labor.criminal;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Slums buy and sell items depending on availabity and the district's
 * population.
 *
 * @author alex
 */
public class Slums extends Location{
	public static class BuildSlums extends Build{
		public BuildSlums(){
			super("Build slums",5,null,Rank.VILLAGE);
		}

		@Override
		public Location getgoal(){
			return new Slums();
		}

		@Override
		public boolean validate(District d){
			return super.validate(d)
					&&d.getlocationtype(Slums.class).size()<d.town.getrank().rank;
		}
	}

	Item item=null;

	public Slums(){
		super("Slums");
		allowentry=false;
		discard=false;
		gossip=true;
	}

	@Override
	public Integer getel(Integer attackerel){
		return Integer.MIN_VALUE;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public void turn(long time,WorldScreen world){
		if(RPG.chancein(7)) if(item==null)
			item=generateitem();
		else
			item=null;
	}

	Item generateitem(){
		int limit=getlimit();
		List<Item> items=new ArrayList<>();
		for(Item i:Item.ALL){
			if(i.price>limit) break;
			items.add(i.clone());
		}
		items=Item.randomize(items);
		return items.isEmpty()?null:RPG.pick(items);
	}

	int getlimit(){
		District d=getdistrict();
		return RewardCalculator.calculatenpcequipment(
				d==null?Rank.VILLAGE.maxpopulation:d.town.population);
	}

	@Override
	public boolean hascrafted(){
		return item!=null;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		if(item==null){
			buyitem();
			return true;
		}
		String prompt="Do you want to buy: "+item.toString().toLowerCase()+" ($"
				+Javelin.format(item.price)+")?\n"
				+"Press b to buy or any other key to leave...";
		if(Javelin.prompt(prompt)=='b') if(Squad.active.gold>=item.price){
			Squad.active.gold-=item.price;
			item.grab();
			item=null;
		}else
			Javelin.message("Not enough money...",false);
		return true;
	}

	void buyitem(){
		ArrayList<Item> items=new ArrayList<>();
		ArrayList<String> descriptions=new ArrayList<>();
		int limit=getlimit();
		for(Combatant c:Squad.active.members)
			for(Item i:Squad.active.equipment.get(c)){
				int sellingprice=i.price/2;
				if(sellingprice>limit) continue;
				items.add(i);
				String equipped=c.equipped.contains(i)?", equipped":"";
				descriptions.add(i+" ("+c+equipped+") $"+Javelin.format(sellingprice));
			}
		int choice=Javelin.choose("Do you want to sell one of your items?",
				descriptions,true,false);
		if(choice>=0){
			Item i=items.get(choice);
			Squad.active.gold+=i.price/2;
			Squad.active.equipment.remove(i);
			item=i;
		}
	}
}
