package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Loot! A chest should normally contain only items or gold, not both.
 *
 * @author alex
 */
public class Chest extends Feature{
	/** Items inside the chest. */
	public ItemSelection items=new ItemSelection();
	/** Gold inside the chest. */
	public int gold=0;

	/** Constructor. */
	Chest(){
		super("dungeonchest");
	}

	/**
	 * @param pool Value to be added in gold or {@link Item}s, preferrring
	 *          generated items. If zero, will not generate anything.
	 * @param generateitems If <code>true</code> and a positive pool, will
	 *          generate items.
	 * @see RewardCalculator#generateloot(int)
	 */
	public Chest(int pool,boolean generateitems){
		this();
		if(generateitems&&pool>0) items.addAll(RewardCalculator.generateloot(pool));
		if(items.isEmpty()) gold=Javelin.round(pool);
	}

	/** @param special If true, will show a distinguished chest image. */
	public Chest(Item i,boolean special){
		this();
		items.add(i);
		if(special) avatarfile="dungeonchestspecial";
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
		return "Floor "+Dungeon.active.getfloor()+" chest: "
				+(items.isEmpty()?"$"+Javelin.format(gold):items);
	}
}
