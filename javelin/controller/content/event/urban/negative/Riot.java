package javelin.controller.content.event.urban.negative;

import javelin.controller.content.event.urban.UrbanEvent;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.old.RPG;

/**
 * Stops {@link Town} {@link Labor}s for a few days.
 *
 * @author alex
 */
public class Riot extends UrbanEvent{
	int strike=town.population+RPG.randomize(town.population);

	/** Reflection constructor. */
	public Riot(Town t){
		super(t,null,Rank.VILLAGE);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return strike>town.strike&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var action=town.strike==0?"start":"are renewed";
		town.strike=strike;
		notify("Riots "+action+" in "+town+"!");
	}
}
