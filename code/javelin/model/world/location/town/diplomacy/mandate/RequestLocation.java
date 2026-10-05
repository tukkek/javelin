package javelin.model.world.location.town.diplomacy.mandate;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.branch.temple.Temple.TempleEntrance;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * Requests that one or more undiscovered {@link Actor}s be revealed.
 *
 * @author alex
 * @see Actor#see()
 */
public class RequestLocation extends Mandate{
	Actor target=null;

	/** Reflection constructor. */
	public RequestLocation(Town t){
		super(t);
	}

	Actor find(Class<? extends Actor> type){
		var found=World.getactors().stream()
				.filter(a->type.isInstance(a)&&!a.cansee())
				.collect(Collectors.toList());
		return found.isEmpty()?null:RPG.pick(found);
	}

	@Override
	public void define(){
		for(var type:List.of(Town.class,TempleEntrance.class,Haunt.class)){
			target=find(type);
			if(target!=null) break;
		}
		super.define();
	}

	@Override
	public boolean validate(){
		return target!=null&&!target.cansee();
	}

	@Override
	public String getname(){
		return "Reveal location of "+target;
	}

	@Override
	public void act(){
		target.reveal();
		Javelin.redraw();
		Javelin.message("The location of "+target+" is revealed!",true);
	}
}
