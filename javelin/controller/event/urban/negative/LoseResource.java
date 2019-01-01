package javelin.controller.event.urban.negative;

import javelin.controller.event.urban.UrbanEvent;
import javelin.model.unit.Squad;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * {@link Town} loses access to a {@link Resource}. Does not happen to
 * {@link Trait#NATURAL} towns.
 *
 * TODO since this is kinda offputting, maybe at least balance it with
 * generating the same resource somewhere in the map?
 *
 * @see Town#resources
 * @author alex
 */
public class LoseResource extends UrbanEvent{
	/** Reflection constructor. */
	public LoseResource(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return !town.resources.isEmpty()&&!town.traits.contains(Trait.NATURAL)
				&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var r=RPG.pick(town.resources);
		town.resources.remove(r);
		notify(town+" lost access to its "+r+"!");
	}
}
