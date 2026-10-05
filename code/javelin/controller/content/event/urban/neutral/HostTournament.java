package javelin.controller.content.event.urban.neutral;

import javelin.controller.content.event.urban.UrbanEvent;
import javelin.controller.content.fight.tournament.Exhibition;
import javelin.controller.content.fight.tournament.Match;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Hosts a number of {@link Exhibition}s at a {@link Town}.
 *
 * @author alex
 */
public class HostTournament extends UrbanEvent{
	/** Reflection constructor. */
	public HostTournament(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return !town.ishostile()&&!town.ishosting()&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		for(int events=RPG.r(1,4)+town.getrank().rank;events>0;events--){
			var e=RPG.r(1,2)==1?RPG.pick(Exhibition.SPECIALEVENTS):new Match();
			town.exhibitions.add(e);
		}
		town.events.add(town+" is hosting a tournament!");
	}
}
