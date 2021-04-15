package javelin.model.world.location.town.diplomacy.mandate;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.old.RPG;

/**
 * Connects a {@link Resource} from a town that has it to a town that doesn't.
 *
 * @author alex
 */
public class RequestTrade extends Mandate{
	Town destination;
	Resource type;

	/** Reflection constructor. */
	public RequestTrade(Town t){
		super(t);
		var candidates=RPG.shuffle(new ArrayList<>(Town.getdiscovered()));
		candidates.remove(t);
		for(var c:candidates)
			if(!c.ishostile()) for(var type:t.resources)
				if(!c.resources.contains(type)){
					destination=c;
					this.type=type;
					return;
				}
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.diplomacy.getstatus()>=0&&destination!=null&&type!=null
				&&target.resources.contains(type)
				&&!destination.resources.contains(type);
	}

	@Override
	public String getname(){
		return "Establish "+type.toString().toLowerCase()+" trade route from "
				+target+" to "+destination;
	}

	@Override
	public void act(Diplomacy d){
		destination.resources.add(type);
		String message="Trade route established, "+target+" gets "
				+type.toString().toLowerCase()+"!";
		destination.diplomacy.reputation+=d.town.population;
		var status=destination.diplomacy.describestatus().toLowerCase();
		message+="\nNew relationship status with "+target+": "+status+".";
		Javelin.message(message,true);
	}
}
