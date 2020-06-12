package javelin.controller.event.urban.neutral;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.event.urban.UrbanEvent;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * {@link Town#population} moves from one unhappy town to another town.
 *
 * @author alex
 */
public class Migration extends UrbanEvent{
	List<Town> destinations;

	/** Reflection constructor. */
	public Migration(Town t){
		super(t,null,Rank.HAMLET);
	}

	static boolean isunhappy(Town t){
		return t.diplomacy.reputation<0;
	}

	@Override
	public boolean validate(Squad s,int squadel){
		if(town.population==1||!isunhappy(town)||!super.validate(s,squadel))
			return false;
		var here=town.getlocation();
		var towns=Town.gettowns();
		towns.remove(town);
		destinations=towns.stream().filter(t->t.see()&&!isunhappy(t))
				.sorted((a,b)->Double.compare(here.distance(a.getlocation()),
						here.distance(b.getlocation())))
				.collect(Collectors.toList());
		return !destinations.isEmpty();
	}

	@Override
	public void happen(Squad s){
		var destination=0;
		while(RPG.chancein(2)&&destination+1<destinations.size())
			destination+=1;
		var from=town;
		var to=destinations.get(destination);
		from.population-=1;
		to.population+=1;
		notify=notify||from.notifyplayer()||to.notifyplayer();
		notify(
				"Looking for better living conditions, a large group of citizens from "
						+from+" relocate to "+to+".");
	}
}
