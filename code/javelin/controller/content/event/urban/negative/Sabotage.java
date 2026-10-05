package javelin.controller.content.event.urban.negative;

import java.util.List;

import javelin.controller.content.event.urban.UrbanEvent;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * Cancels a {@link Town} {@link Labor}.
 *
 * @author alex
 */
public class Sabotage extends UrbanEvent{
	/** Reflection constructor. */
	public Sabotage(Town t){
		super(t,List.of(Trait.CRIMINAL),Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return town.getgovernor().countprojects()>0&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var p=RPG.pick(town.getgovernor().getprojects());
		p.cancel();
		notify("A project in "+town+" falls victim to sabotage ("
				+p.toString().toLowerCase()+")!");
	}
}
