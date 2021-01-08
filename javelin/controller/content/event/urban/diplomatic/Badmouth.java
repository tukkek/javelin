package javelin.controller.content.event.urban.diplomatic;

import javelin.model.town.diplomacy.Diplomacy;
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
		return super.validate(s,squadel)&&town.diplomacy.getstatus()>-1;
	}

	@Override
	public void happen(Squad s){
		var reputation=town.population+RPG.randomize(town.population);
		town.diplomacy.reputation-=reputation;
		notify("Someone in "+town+" is badmouthing you!\nYou lose reputation!");
	}
}
