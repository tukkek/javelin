package javelin.model.diplomacy.mandate;

import java.util.stream.Collectors;

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
		var p=target.town.population;
		var candidates=Terrain.get(l.x,l.y).getmonsters().stream()
				.filter(m->p-4<=m.cr&&m.cr<=p).collect(Collectors.toList());
		return candidates.isEmpty()?null:RPG.pick(candidates);
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.getstatus()==Relationship.ALLY&&getsquad()!=null
				&&getally()!=null;
	}

	@Override
	public String getname(){
		return "Request unit from "+target;
	}

	@Override
	public void act(Diplomacy d){
		getsquad().add(new Combatant(getally(),true));
	}
}
