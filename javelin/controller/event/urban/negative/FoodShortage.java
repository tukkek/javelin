package javelin.controller.event.urban.negative;

import javelin.Javelin;
import javelin.controller.event.urban.UrbanEvent;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;

/**
 * Reduces {@link Town#population} in {@link Season#WINTER}.
 * {@link Trait#NATURAL} immune to it.
 *
 * @author alex
 */
public class FoodShortage extends UrbanEvent{
	/** Reflection constructor. */
	public FoodShortage(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return Season.current==Season.WINTER&&town.population>1
				&&!town.traits.contains(Trait.NATURAL)&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		town.population-=1;
		if(notify) Javelin
				.message("The rough winter causes a food shortage in "+town+"...",true);
	}
}
