/**
 *
 */
package javelin.model.world.location.town.diplomacy.mandate;

import java.util.stream.Collectors;

import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Item;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Rewards an {@link Item} based on {@link Town#populate(int)}.
 */
public class RequestItem extends Mandate{
	Item item;

	/** Constructor. */
	public RequestItem(Town t){
		super(t);
	}

	@Override
	public void define(){
		var p=town.population;
		var from=RewardCalculator.getgold(p-1);
		var to=RewardCalculator.getgold(p);
		var candidates=Item.ITEMS.stream().filter(i->from<=i.price&&i.price<=to)
				.collect(Collectors.toList());
		if(!candidates.isEmpty()) item=RPG.pick(candidates);
		super.define();
	}

	@Override
	public String getname(){
		return "Request "+item.toString().toLowerCase();
	}

	@Override
	public boolean validate(){
		return item!=null;
	}

	@Override
	public void act(){
		item.clone().grab();
	}
}
