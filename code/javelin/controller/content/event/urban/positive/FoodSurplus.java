package javelin.controller.content.event.urban.positive;

import java.util.List;

import javelin.controller.content.event.urban.UrbanEvent;
import javelin.controller.content.event.urban.negative.FoodShortage;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;

/**
 * Increases {@link Town#population} in {@link Trait#NATURAL} {@link Town}s
 * during {@link Season#SPRING}.
 *
 * @author alex
 * @see FoodShortage
 */
public class FoodSurplus extends UrbanEvent{
	/** Reflection constructor. */
	public FoodSurplus(Town t){
		super(t,List.of(Trait.NATURAL),Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return town.population<20&&town.traits.contains(Trait.NATURAL)
				&&Season.current==Season.SPRING&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		town.population+=1;
		notify("The harvests this year have blessed "+town
				+" with a bountiful surplus!");
	}
}
