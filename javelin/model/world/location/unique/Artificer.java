package javelin.model.world.location.unique;

import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.gear.Gear;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Period;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.Location;
import javelin.model.world.location.order.CraftingOrder;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;
import javelin.old.RPG;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.ArtificerScreen;
import javelin.view.screen.town.PurchaseOption;

/**
 * Allows a player to forge and sell artifacts.
 *
 * @author alex
 */
public class Artificer extends Fortification{
	static final String DESCRIPTION="An artificer";
	static final boolean DEBUG=false;

	public static class BuildArtificer extends Build{
		public BuildArtificer(){
			super("Build artificer",20,Rank.CITY,null);
		}

		@Override
		public Location getgoal(){
			return new Artificer();
		}
	}

	/**
	 * {@link Gear}s this artificer can craft. This generically represents the
	 * base components the Artificer has at the moment to create this selection of
	 * magic items. Will randomly replace an item once per month, representing not
	 * only a possible sell but also getting new alchemical components, old ones
	 * spoiling, etc.
	 */
	public ItemSelection selection=new ItemSelection();
	CraftingOrder crafting=null;

	/** Constructor. */
	public Artificer(){
		super(DESCRIPTION,DESCRIPTION,11,15);
		vision=1;
		gossip=true;
		while(selection.size()<9)
			stock();
		if(DEBUG) garrison.clear();
	}

	void stock(){
		int i=0;
		while(!selection.add(RPG.pick(Item.GEAR))){
			// wait until 1 item enters
			i+=1;
			if(i>=10000){
				if(Javelin.DEBUG)
					throw new RuntimeException("Cannot generate artificer item!");
				break; // tough luck :/
			}
		}
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		if(crafting==null){
			new ArtificerScreen(this).show();
			return true;
		}
		if(crafting.completed(Period.gettime())){
			crafting.item.grab();
			crafting=null;
			return true;
		}
		MessagePanel.active.clear();
		Javelin.message("\"I still need "+crafting.geteta(Period.gettime())
				+" before your "+crafting.item.toString().toLowerCase()
				+" is completed.\"",false);
		Javelin.input();
		return true;
	}

	@Override
	public void turn(long time,WorldScreen world){
		if(RPG.random()<=1/30f){
			selection.remove(RPG.pick(selection));
			stock();
		}
	}

	@Override
	public boolean hascrafted(){
		return crafting!=null&&crafting.completed(Period.gettime());
	}

	/**
	 * @param o Start crafting this {@link PurchaseOption#i} .
	 */
	public void craft(PurchaseOption o){
		selection.remove(o.i);
		crafting=new CraftingOrder(o.i,null);
		stock();
	}

	@Override
	public List<Combatant> getcombatants(){
		return garrison;
	}

	@Override
	public boolean isworking(){
		return crafting!=null&&!crafting.completed(Period.gettime());
	}
}
