package javelin.controller.event.urban.neutral;

import javelin.Javelin;
import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.fight.tournament.Match;
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
		Javelin.message(town+" is now hosting a tournament!",true);
	}
}
