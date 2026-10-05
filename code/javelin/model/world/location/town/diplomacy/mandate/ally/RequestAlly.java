package javelin.model.world.location.town.diplomacy.mandate.ally;

import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.mandate.Mandate;
import javelin.old.RPG;

/**
 * Adds a non-mercenary {@link Combatant} to a {@link Squad} inside the
 * {@link Town} {@link District}.
 *
 * @author alex
 * @see #getsquad()
 */
public abstract class RequestAlly extends Mandate{
	Monster ally;

	/** Reflection constructor. */
	public RequestAlly(Town t){
		super(t);
	}

	@Override
	public void define(){
		var l=town.getlocation();
		var all=Terrain.get(l.x,l.y).getmonsters();
		for(var el=town.population;el>0;el--){
			final var cr=el;
			var candidates=all.stream().filter(m->m.cr==cr&&filter(m))
					.collect(Collectors.toList());
			if(!candidates.isEmpty()){
				ally=RPG.pick(candidates);
				break;
			}
		}
		super.define();
	}

	/** @return <code>true</code> if unit is a valid recruit. */
	protected abstract boolean filter(Monster m);

	@Override
	public boolean validate(){
		return ally!=null;
	}

	@Override
	public String getname(){
		return "Request unit ("+ally+")";
	}

	@Override
	public void act(){
		Squad.active.recruit(ally);
		Javelin.message(ally+" joins your ranks!",true);
	}
}
