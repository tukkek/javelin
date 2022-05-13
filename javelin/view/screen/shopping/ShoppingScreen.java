package javelin.view.screen.shopping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.trigger.Wand;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseOption;
import javelin.view.screen.town.PurchaseScreen;

/**
 * Allows player to buy items.
 *
 * @author alex
 */
public abstract class ShoppingScreen extends PurchaseScreen{
	/** Constructor. */
	public ShoppingScreen(String s,Town t){
		super(s,t);
	}

	/**
	 * @param o Called after an option has been acquired.
	 */
	protected abstract void afterpurchase(final PurchaseOption o);

	/**
	 * @return Available items.
	 */
	protected abstract ItemSelection getitems();

	@Override
	public List<Option> getoptions(){
		final ArrayList<Option> list=new ArrayList<>();
		for(final Item i:getitems())
			list.add(new PurchaseOption(i));
		return list;
	}

	@Override
	public boolean select(final Option op){
		if(!canbuy(op)){
			text+="Not enough gold...\n";
			return false;
		}
		final PurchaseOption o=(PurchaseOption)op;
		spend(o);
		afterpurchase(o);
		return true;
	}

	@Override
	protected boolean canbuy(Option o){
		return getgold()>=((PurchaseOption)o).i.price;
	}

	@Override
	protected void spend(final Option o){
		Squad.active.gold-=((PurchaseOption)o).i.price;
	}

	@Override
	public String printpriceinfo(Option o){
		Item i=((PurchaseOption)o).i;
		String useinfo="";
		if(i instanceof Wand||i instanceof Scroll){
			ArrayList<Combatant> members=new ArrayList<>(getbuyers());
			for(Combatant c:new ArrayList<>(members))
				if(i.canuse(c)!=null) members.remove(c);
			if(members.isEmpty())
				useinfo=" - can't use";
			else{
				useinfo=" - can use: ";
				for(Combatant c:members)
					useinfo+=c+", ";
				useinfo=useinfo.substring(0,useinfo.length()-2);
			}
		}
		return " ("+super.printpriceinfo(o).substring(1)+")"+useinfo;
	}

	protected List<Combatant> getbuyers(){
		return Squad.active.members;
	}

	@Override
	public String printinfo(){
		return "You squad has $"+Javelin.format(getgold())+".";
	}

	@Override
	protected int getgold(){
		return Squad.active.gold;
	}

	@Override
	protected Comparator<Option> sort(){
		return (a,b)->-Double.compare(a.price,b.price);
	}
}