package javelin.model.world.location.order;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.item.potion.Potion;
import javelin.model.world.World;

/**
 * {@link Item} in the process of being completed.
 *
 * @author alex
 */
public class CraftingOrder extends Order{
	/** Item to be done at {@link Order#completionat}. */
	public Item item;

	public CraftingOrder(Item i,OrderQueue queue){
		super(i instanceof Potion?24:Math.max(24,24*i.price/1000),i.name);
		item=i.clone();
		if(queue!=null&&!queue.queue.isEmpty()){
			long hours=queue.last().completionat-Javelin.gettime();
			completionat+=hours/World.scenario.boost;
		}
	}
}
