package javelin.model.world.location.town.diplomacy.mandate;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Connects a {@link Resource} from a town that has it to a town that doesn't.
 *
 * @author alex
 */
public class RequestTrade extends Mandate{
	Town with;
	Resource type;

	/** Reflection constructor. */
	public RequestTrade(Town t){
		super(t);
	}

	Resource trade(Town from,Town to){
		var incoming=new ArrayList<>(from.resources);
		incoming.removeAll(to.resources);
		incoming.sort(null);
		return incoming.isEmpty()?null:incoming.get(0);
	}

	Resource trade(Town t){
		var resource=trade(t,town);
		if(resource!=null) return resource;
		return trade(town,t);
	}

	boolean validate(Town t){
		return t!=town&&t.cansee()&&!t.ishostile()&&trade(t)!=null;
	}

	@Override
	public void define(){
		var towns=Town.getdiscovered().stream().filter(t->validate(t))
				.collect(Collectors.toList());
		if(towns.isEmpty()) return;
		with=RPG.pick(towns);
		type=trade(with);
		super.define();
	}

	@Override
	public String getname(){
		return "Establish "+type.toString().toLowerCase()+" trade route with "+with;
	}

	@Override
	public boolean validate(){
		return type!=null&&validate(with)&&trade(with)==type;
	}

	@Override
	public void act(){
		town.resources.add(type);
		with.resources.add(type);
		var m="Trade route for %s established between %s and %s!";
		var t=type.toString().toLowerCase();
		Javelin.message(String.format(m,t,town,with),true);
	}
}
