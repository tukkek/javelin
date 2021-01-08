package javelin.controller.content.event.urban.positive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.content.event.urban.UrbanEvent;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.location.ResourceSite;
import javelin.model.world.location.ResourceSite.Resource;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * If {@link Town} is {@link Trait#NATURAL}, gain a new, relevant
 * {@link Resource} to its surroundings.
 *
 * @author alex
 */
public class GainResource extends UrbanEvent{
	Set<Resource> available=new HashSet<>(ResourceSite.RESOURCES.size());

	/** Reflection constructor. */
	public GainResource(Town t){
		super(t,List.of(Trait.NATURAL),Rank.HAMLET);
		for(var tile:town.getdistrict().getarea())
			available.add(ResourceSite.RESOURCES.get(Terrain.get(tile.x,tile.y)));
		available.removeAll(town.resources);

	}

	@Override
	public boolean validate(Squad s,int squadel){
		return !available.isEmpty()&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var r=RPG.pick(available);
		town.resources.add(r);
		notify(town+" has found access to a new resource: "+r+"!");
	}
}
