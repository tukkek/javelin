package javelin.controller.content.event.urban;

import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

/**
 * TODO used to pad all 3 {@link UrbanEvents} queues since they all need at
 * least 1 always-valid card type.
 *
 * @author alex
 */
public class NothingHappens extends UrbanEvent{
	/** Reflection constructor. */
	public NothingHappens(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public void happen(Squad s){
		//literally nothing happens yo
	}
}
