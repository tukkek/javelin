package javelin.model.diplomacy.mandate;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.old.RPG;

/**
 * Requests that one or more undiscovered {@link Actor}s be revealed.
 *
 * @author alex
 * @see Actor#see()
 */
public class RequestLocation extends Mandate{
	/** Reflection constructor. */
	public RequestLocation(Relationship r){
		super(r);
	}

	boolean validate(Actor a){
		var t=target.town;
		var realm=t.realm;
		if(realm==null||!realm.equals(a.realm)) return false;
		var population=target.town.population;
		var el=a.getel(population);
		if(el==null||el>population) return false;
		return !a.see();
	}

	List<Actor> gettargets(){
		return World.getactors().stream().filter(a->validate(a))
				.collect(Collectors.toList());
	}

	int getamount(){
		int amount=target.getabsolutestatus()-1;
		if(amount<=0) return amount;
		amount+=RPG.randomize(amount);
		return Math.min(gettargets().size(),amount);
	}

	@Override
	public boolean validate(Diplomacy d){
		return getamount()>0;
	}

	@Override
	public String getname(){
		return "Reveal location(s) aligned with "+target.town;
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
