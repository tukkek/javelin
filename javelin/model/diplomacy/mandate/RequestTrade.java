package javelin.model.diplomacy.mandate;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
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
	public RequestTrade(Relationship r){
		super(r);
		var candidates=RPG
				.shuffle(new ArrayList<>(Diplomacy.instance.getdiscovered().keySet()));
		candidates.remove(r.town);
		for(var c:candidates)
			if(!c.ishostile()) for(var type:r.town.resources)
				if(!c.resources.contains(type)){
					destination=c;
					this.type=type;
					return;
				}
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.getstatus()==Relationship.ALLY&&destination!=null&&type!=null
				&&target.town.resources.contains(type)
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
		var relationship=Diplomacy.instance.getdiscovered().get(destination);
		String message="Trade route established, "+target+" gets "
				+type.toString().toLowerCase()+"!";
		if(relationship!=null&&relationship.changestatus(+1)){
			var status=relationship.describestatus().toLowerCase();
			message+="\nNew relationship status with "+target+": "+status+".";
		}
		Javelin.message(message,true);
	}
}
