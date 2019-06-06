package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Loot!
 *
 * @author alex
 */
public class Chest extends Feature{
	/**
	 * TODO it's OK for Chests go only give a single item
	 */
	public ItemSelection items=new ItemSelection();
	public int gold=0;
	public boolean special=false;

	/**
	 * @param visual Name of this {@link Thing}.
	 * @param x Location.
	 * @param y Location.
	 * @param goldp Gold loot. See {@link Squad#gold}.
	 * @param itemsp {@link Item} loot.
	 */
	public Chest(int x,int y){
		super(x,y,"dungeonchest");
	}

	/**
	 * @param pool Value to be added in gold or {@link Item}s, preferrring
	 *          generated items.
	 * @param p {@link Dungeon} coordinate.
	 * @param forbidden Do not generate these item types.
	 */
	public Chest(int pool,Point p){
		this(p.x,p.y);
		items.addAll(RewardCalculator.generateloot(pool));
		gold=items.isEmpty()?Javelin.round(pool):0;
	}

	/** Constructor. */
	public Chest(int x,int y,Item i){
		this(x,y);
		items.add(i);
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

	public void setspecial(){
		special=true;
		avatarfile="dungeonchestspecial";
	}

	@Override
	public String toString(){
		return "Floor "+Dungeon.active.floor+" chest: "
				+(items.isEmpty()?"$"+Javelin.format(gold):items);
	}
}
