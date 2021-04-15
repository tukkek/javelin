package javelin.model.world.location.town.diplomacy.mandate.unit;

import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.diplomacy.mandate.Mandate;
import javelin.old.RPG;

/**
 * Adds mercenaries to a Squad in the relevant {@link District}.
 *
 * @see #getsquad()
 * @author alex
 */
public class RequestMercenaries extends Mandate{
	/** Reflection constructor. */
	public RequestMercenaries(Town t){
		super(t);
	}

	@Override
	public boolean validate(Diplomacy d){
		return getsquad()!=null&&getmercenaries()!=null;
	}

	Encounter getmercenaries(){
		var l=target.getlocation();
		var t=Terrain.get(l.x,l.y).name;
		var mercenaries=Organization.ENCOUNTERSBYTERRAIN.get(t)
				.get(target.population);
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
