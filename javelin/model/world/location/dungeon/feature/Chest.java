package javelin.model.world.location.dungeon.feature;

import java.util.Collection;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

/**
 * Loot! A chest should normally contain only items or gold, not both.
 *
 * TODO would be pretty cool to have rarer but specialized types of chests such
 * as alchemy tables (potions), weapon racks (weapons), bookshelves (scrolls)...
 *
 * @author alex
 */
public class Chest extends Feature{
	/** Items inside the chest. */
	public ItemSelection items=new ItemSelection();
	/** Gold inside the chest. */
	public int gold=0;

	/** Amount of items to generate. Random if <code>null</code>. */
	public Integer nitems=null;

	/**
	 * @param pool Value to be added in gold or {@link Item}s, preferrring
	 *          generated items. If zero, will not generate anything.
	 * @param generateitems If <code>true</code> and a positive pool, will
	 *          generate items.
	 * @see RewardCalculator#generateloot(int)
	 */
	public Chest(Integer pool){
		super("dungeonchest");
		gold=pool;
		if(nitems==null){
			nitems=1;
			while(RPG.chancein(2))
				nitems+=1;
		}
	}

	/** @param special If true, will show a distinguished chest image. */
	public Chest(Item i,boolean special){
		this(0);
		items.add(i);
		if(special) avatarfile="dungeonchestspecial";
	}

	/**
	 * @return A list of all items that can be found. Will eventually be passed to
	 *         {@link Item#randomize()}.
	 */
	protected Collection<Item> getitems(){
		return Item.ITEMS;
	}

	@Override
	public void place(Dungeon d,Point p){
		if(x>=0){
			super.place(d,p);
			return;
		}
		if(nitems>0&&items.isEmpty()&&gold>0)
			items.addAll(RewardCalculator.generateloot(gold,nitems,getitems()));
		if(items.isEmpty()) gold=Javelin.round(gold);
		if(!items.isEmpty()||gold>0) super.place(d,p);
	}

	@Override
	public boolean activate(){
		if(items.isEmpty()){
			if(gold<1) gold=1;
			String message="Party receives $"+Javelin.format(gold)+"!";
			Javelin.message(message,false);
			Squad.active.gold+=gold;
		}else
			for(Item i:items)
				i.grab();
		return true;
	}

	@Override
	public String toString(){
		return getClass().getSimpleName()+": "
				+(items.isEmpty()?"$"+Javelin.format(gold):items);
	}
}
