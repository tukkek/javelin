package javelin.controller.event.urban.diplomatic;

import javelin.Javelin;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * In an unhappy {@link Town}, someone taints your {@link Diplomacy#reputation}.
 *
 * @author alex
 * @see Praise
 */
public class Badmouth extends DiplomaticEvent{
	/** Reflection constructor. */
	public Badmouth(Town t){
		super(t,null,Rank.TOWN);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		if(!super.validate(s,squadel)||Diplomacy.instance.reputation==0)
			return false;
		var h=town.describehappiness();
		return h==Town.UNHAPPY||h==Town.REVOLTING;
	}

	@Override
	public void happen(Squad s){
		var reputation=town.population+RPG.randomize(town.population);
		reputation=Math.min(reputation,Diplomacy.instance.reputation);
		Diplomacy.instance.reputation-=reputation;
		if(notify) Javelin.message("Someone in "+town+" is badmouthing you!\n"
				+"You lose "+reputation+" reputation.",true);
	}
}
