package javelin.model.diplomacy.mandate;

import javelin.controller.db.reader.fields.Organization;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.terrain.Terrain;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.world.location.town.District;
import javelin.old.RPG;

/**
 * Adds mercenaries to a Squad in the relevant {@link District}.
 *
 * @see #getsquad()
 * @author alex
 */
public class RequestMercenaries extends Mandate{
	/** Reflection constructor. */
	public RequestMercenaries(Relationship r){
		super(r);
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.status>=Relationship.INDIFFERENT&&getsquad()!=null
				&&getmercenaries()!=null;
	}

	Encounter getmercenaries(){
		var l=target.town.getlocation();
		var t=Terrain.get(l.x,l.y).name;
		var mercenaries=Organization.ENCOUNTERSBYTERRAIN.get(t)
				.get(target.town.population);
		return mercenaries==null||mercenaries.isEmpty()?null:RPG.pick(mercenaries);
	}

	@Override
	public String getname(){
		return "Request mercenaries from "+target;
	}

	@Override
	public void act(Diplomacy d){
		var mercenaries=getmercenaries().generate();
		for(var m:mercenaries)
			m.setmercenary(true);
		getsquad().members.addAll(mercenaries);
	}
}
