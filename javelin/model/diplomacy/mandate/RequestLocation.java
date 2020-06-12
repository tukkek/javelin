package javelin.model.diplomacy.mandate;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Requests that one or more undiscovered {@link Actor}s be revealed.
 *
 * @author alex
 * @see Actor#see()
 */
public class RequestLocation extends Mandate{
	/** Reflection constructor. */
	public RequestLocation(Town t){
		super(t);
	}

	boolean validate(Actor a){
		var realm=target.realm;
		if(realm==null||!realm.equals(a.realm)) return false;
		var population=target.population;
		var el=a.getel(population);
		if(el==null||el>population) return false;
		return !a.see();
	}

	List<Actor> gettargets(){
		return World.getactors().stream().filter(a->validate(a))
				.collect(Collectors.toList());
	}

	int getamount(){
		return Math.min(gettargets().size(),target.getrank().rank);
	}

	@Override
	public boolean validate(Diplomacy d){
		return getamount()>0;
	}

	@Override
	public String getname(){
		return "Reveal location(s) aligned with "+target;
	}

	@Override
	public void act(Diplomacy d){
		var targets=RPG.shuffle(gettargets()).subList(0,getamount());
		for(var a:targets)
			a.reveal();
		Javelin.redraw();
		var revealed="The locations of the following are revealed:\n"
				+targets.stream().map(a->a.toString()).sorted()
						.collect(Collectors.joining(", "))
				+".";
		Javelin.message(revealed,true);
	}
}
