package javelin.model.world.location.dungeon.feature;

import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.Tier;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Provides an {@link Item} than can be used to add a new option to a
 * {@link Shop}.
 *
 * TODO update image to a pedestal or something that makes sense in the context
 * of a {@link DungeonFloor}
 *
 * @author alex
 */
public class Recipe extends Feature{
	class CraftingRecipe extends Item{
		static final String INSTRUCTIONS="Use this recipe near a shop to allow the shop to craft the item on demand.";

		CraftingRecipe(){
			super("Crafting recipe for "+item.name,item.price,false);
			consumable=true;
			usedinbattle=false;
			usedoutofbattle=true;
			targeted=false;
		}

		Shop findshop(){
			if(WorldScreen.current.getClass()!=WorldScreen.class) return null;
			var s=Squad.active;
			var d=s.getdistrict();
			if(d==null) return null;
			return (Shop)d.getlocationtype(Shop.class).stream()
					.filter(shop->shop.distanceinsteps(s.x,s.y)<=1).findAny()
					.orElse(null);
		}

		@Override
		public boolean usepeacefully(Combatant user){
			var shop=findshop();
			if(shop==null) return false;
			shop.add(item);
			var t=shop.getdistrict().town.toString();
			var success=String.format("Added to the %s %s:\n%s!",t,
					shop.toString().toLowerCase(),item.name);
			Javelin.message(success,true);
			return true;
		}

		@Override
		public String describefailure(){
			return INSTRUCTIONS;
		}
	}

	Item item=null;

	/** Constructor. */
	public Recipe(int dungeonlevel){
		super("recipe");
		var from=RewardCalculator.getgold(dungeonlevel-1);
		var to=RewardCalculator.getgold(dungeonlevel+1);
		var candidates=Item.NONPRECIOUS.stream()
				.filter(i->from>=i.price&&i.price<=to)
				.filter(i->!(i instanceof RuneGear)).collect(Collectors.toList());
		if(!candidates.isEmpty()) item=RPG.pick(candidates);
	}

	/** Constructor. */
	public Recipe(){
		this(Dungeon.active.level);
	}

	@Override
	public boolean validate(){
		return super.validate()&&item!=null;
	}

	@Override
	public boolean activate(){
		new CraftingRecipe().grab();
		return true;
	}

	/**
	 * Helper method to determine which {@link DungeonFloor} levels can generate
	 * recipes.
	 *
	 * @see Debug
	 */
	public static void test(){
		for(var level=Tier.LOW.minlevel;level<=Tier.EPIC.maxlevel;level++){
			var valid=new Recipe(level).validate()?"valid":"invalid";
			System.out.println(String.format("Recippe level %s: %s.",level,valid));
		}
	}

	@Override
	public String toString(){
		return "Recipe ("+item.name.toLowerCase()+")";
	}
}
