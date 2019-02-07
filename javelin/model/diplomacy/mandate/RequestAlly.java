package javelin.model.diplomacy.mandate;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Adds a non-mercenary {@link Combatant} to a {@link Squad} inside the
 * {@link Town} {@link District}.
 *
 * @author alex
 * @see #getsquad()
 */
public class RequestAlly extends Mandate{
	/** Reflection constructor. */
	public RequestAlly(Relationship r){
		super(r);
	}

	Monster getally(){
		var l=target.town.getlocation();
		var all=Terrain.get(l.x,l.y).getmonsters();
		var el=target.town.population+target.getstatus()-Relationship.ALLY;
		for(;el>0;el--){
			final var cr=el;
			var candidates=all.stream().filter(m->m.cr==cr)
					.collect(Collectors.toList());
			if(!candidates.isEmpty()) return RPG.pick(candidates);
		}
		return null;
	}

	@Override
	public boolean validate(Diplomacy d){
		return getsquad()!=null&&getally()!=null;
	}

	@Override
	public String getname(){
		return "Request unit from "+target;
	}

	@Override
	public void act(Diplomacy d){
		var ally=getally();
		getsquad().recruit(ally);
		Javelin.message(ally+" joins your ranks!",true);
	}
}
