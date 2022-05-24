package javelin.model.world.location.order;

import javelin.controller.challenge.Tier;
import javelin.model.item.Item;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.consumable.potion.Potion;
import javelin.model.world.Period;
import javelin.model.world.World;
import javelin.test.TestShop;

/**
 * {@link Item} in the process of being completed.
 *
 * TODO would be cool to have best-scenario ETAs: for example, if an item takes
 * 2 hours to complete, it takes only two hours but the Shop cannot create
 * another item for the day - that would require tweaking multiple core systems
 * though, including how {@link World} handles time and how
 * {@link CraftingOrder}s are updated.
 *
 * TODO a simpler but maybe even better idea would be to have 1 item be crafted
 * simultaneously per Town {@link Tier} - which would not decrease the overall
 * timing for a single {@link Item} but would decrease total time for multiple
 * orders by a lot.
 *
 * @author alex
 */
public class CraftingOrder extends Order{
  static final int DAY=24;
  static final int MAXWORK=8;

  /** Item to be done at {@link Order#completionat}. */
  public Item item;

  /** @see TestShop */
  public static int calculateeta(Item i){
    var hours=DAY/MAXWORK;
    if(i.price<=250&&(i instanceof Potion||i instanceof Scroll)) hours*=2;
    else if(i.price<1000) hours*=4;
    else hours*=4*i.price/1000;
    return hours<24?24:hours;
  }

  public CraftingOrder(Item i,OrderQueue queue){
    super(calculateeta(i),i.name);
    item=i.clone();
    if(queue!=null&&!queue.queue.isEmpty()){
      var hours=queue.last().completionat-Period.gettime();
      completionat+=hours;
    }
  }
}
