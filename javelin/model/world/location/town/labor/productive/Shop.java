package javelin.model.world.location.town.labor.productive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.Potion;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.order.CraftingOrder;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.PurchaseOption;
import javelin.view.screen.town.SelectScreen;

/**
 * A {@link World} location where you can buy and sell a selection of
 * {@link Item}s.
 *
 * @see ShoppingScreen
 * @author alex
 */
public class Shop extends Location{
	static final Option SELL=new Option("Sell items",0,'s');

	class SellingScreen extends SelectScreen{
		HashMap<Option,Item> selling=new HashMap<>();
		int buylimit=0;

		public SellingScreen(){
			super("Sell which items?",null);
			District d=getdistrict();
			if(d!=null){
				buylimit=RewardCalculator.getgold(d.town.population);
				buylimit=Javelin.round(buylimit);
			}
		}

		@Override
		public String getCurrency(){
			return "$";
		}

		@Override
		public String printinfo(){
			String limit=Javelin.format(buylimit);
			String info="The shop will pay at most $"+limit+" for an item.\n";
			return info+"Your squad has $"+Javelin.format(Squad.active.gold)+".";
		}

		@Override
		public List<Option> getoptions(){
			ArrayList<Option> options=new ArrayList<>();
			for(Combatant c:Squad.active.members)
				for(Item i:Squad.active.equipment.get(c))
					if(i.sell()){
						String listing="["+c+"] "+i.describe(c);
						int sellingprice=Math.min(buylimit,i.price/2);
						Option o=new Option(listing,sellingprice);
						selling.put(o,i);
						options.add(o);
					}
			return options;
		}

		@Override
		public boolean select(Option o){
			Squad.active.gold+=o.price;
			Squad.active.equipment.remove(selling.get(o));
			return true;
		}
	}

	/**
	 * {@link Town} {@link Labor}.
	 *
	 * @author alex
	 */
	public static class BuildShop extends Build{
		/** Constructor. */
		public BuildShop(){
			super("Build shop",5,Rank.HAMLET,null);
		}

		@Override
		public Location getgoal(){
			return new Shop(town.originalrealm,false);
		}

		@Override
		public boolean validate(District d){
			return super.validate(d)&&d.getlocationtype(Shop.class).isEmpty();
		}
	}

	class ShowShop extends ShoppingScreen{
		Shop s;

		ShowShop(Shop s){
			super("You enter the shop.",null);
			this.s=s;
		}

		@Override
		protected ItemSelection getitems(){
			return selection;
		}

		@Override
		protected void afterpurchase(PurchaseOption o){
			s.crafting.add(new CraftingOrder(o.i,crafting));
		}

		@Override
		public String printinfo(){
			String info=super.printinfo();
			if(!crafting.queue.isEmpty())
				info+="\n\nCurrently crafting: "+crafting+'.';
			return info;
		}

		@Override
		public String printpriceinfo(Option o){
			return o.price==0?"":super.printpriceinfo(o);
		}

		@Override
		public List<Option> getoptions(){
			List<Option> options=super.getoptions();
			if(cansell()){
				SELL.priority=2;
				options.add(SELL);
			}
			return options;
		}

		private boolean cansell(){
			if(getdistrict()==null) return false;
			for(ArrayList<Item> bag:Squad.active.equipment.values())
				for(Item i:bag)
					if(i.sell()) return true;
			return false;
		}

		@Override
		public boolean select(Option o){
			if(o==SELL){
				new SellingScreen().show();
				return true;
			}
			return super.select(o);
		}
	}

	class UpgradeShop extends BuildingUpgrade{
		public UpgradeShop(Shop s,int newlevel){
			super("",5,newlevel,s,Rank.HAMLET);
			name="Upgrade shop";
		}

		@Override
		public Location getgoal(){
			return previous;
		}

		@Override
		public boolean validate(District d){
			return cost>0&&crafting.queue.isEmpty()&&super.validate(d);
		}

		@Override
		public void done(){
			super.done();
			level=upgradelevel;
			stock();
		}
	}

	ItemSelection selection=new ItemSelection();
	OrderQueue crafting=new OrderQueue();
	int level=1;
	Realm selectiontype;

	/**
	 * @param r Determines selection of {@link Item}s sold.
	 * @param first If <code>true</code>, will add {@link Potion}s of
	 *          {@link CureLightWounds} to the inventory. Meant to be used at the
	 *          starting {@link Town} as an early-game helper.
	 * @see Realm#getitems()
	 */
	public Shop(Realm r,boolean first){
		super(r.prefixate()+" shop");
		allowentry=false;
		discard=false;
		gossip=true;
		selectiontype=World.scenario.randomrealms?Realm.random():r;
		if(first) selection.add(new Potion(new CureLightWounds()));
		stock();
	}

	void stock(){
		var tier=Tier.TIERS.get(level-1);
		var items=RPG.shuffle(new ArrayList<>(Item.BYTIER.get(tier)));
		for(int i=0;i<items.size()&&i<5;i++)
			selection.add(items.get(i));
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		for(Order o:crafting.reclaim(Squad.active.hourselapsed)){
			CraftingOrder done=(CraftingOrder)o;
			done.item.grab();
		}
		new ShowShop(this).show();
		return true;
	}

	@Override
	public boolean hascrafted(){
		return crafting.reportanydone();
	}

	@Override
	public ArrayList<Labor> getupgrades(District d){
		var upgrades=super.getupgrades(d);
		if(d.town.getrank().rank>level) upgrades.add(new UpgradeShop(this,level+1));
		return upgrades;
	}

	@Override
	public boolean isworking(){
		return !crafting.queue.isEmpty()&&!crafting.reportalldone();
	}

	@Override
	public boolean canupgrade(){
		return super.canupgrade()&&crafting.isempty();
	}
}
