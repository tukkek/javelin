package javelin.controller.event.urban.diplomatic;

import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Someone in a happy {@link Town} is increasing your
 * {@link Diplomacy#reputation}.
 *
 * @see Badmouth
 * @author alex
 */
public class Praise extends DiplomaticEvent{
	/** Reflection constructor. */
	public Praise(Town t){
		super(t,null,Rank.TOWN);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return super.validate(s,squadel)&&town.diplomacy.reputation<1;
	}

	@Override
	public void happen(Squad s){
		town.diplomacy.reputation+=town.population+RPG.randomize(town.population);
		String message="People in "+town+" are celebrating you!";
		notify(message);
	}
}
