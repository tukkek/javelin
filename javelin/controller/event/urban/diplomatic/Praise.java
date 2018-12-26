package javelin.controller.event.urban.diplomatic;

import javelin.Javelin;
import javelin.model.diplomacy.Diplomacy;
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
		return super.validate(s,squadel)
				&&Diplomacy.instance.reputation<Diplomacy.TRIGGER
				&&town.describehappiness()==Town.HAPPY;
	}

	@Override
	public void happen(Squad s){
		var reputation=town.population+RPG.randomize(town.population);
		Diplomacy.instance.reputation+=reputation;
		if(notify){
			String message="People in "+town+" are celebrating you!\n"+"You gain "
					+reputation+" reputation.";
			if(Diplomacy.instance.reputation>=Diplomacy.TRIGGER)
				message+=" You can now make a diplomatic action!";
			Javelin.message(message,true);
		}
	}
}
