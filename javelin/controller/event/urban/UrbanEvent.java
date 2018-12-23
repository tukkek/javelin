package javelin.controller.event.urban;

import javelin.controller.event.EventCard;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

public abstract class UrbanEvent extends EventCard{

	/**
	 * If not-<code>null</code>, requires this {@link Town} trait to be valid.
	 *
	 * @see Town#traits
	 */
	protected String trait;
	/** Minimum city size for this event to be valid. */
	protected Rank minimumrank;
	/** Town this event is happening in. */
	protected Town town;
	/**
	 * Whether to notify players of the event or not. If <code>false</code>,
	 * should not present output of input request.
	 *
	 * If it is impossible to run the event without player interaction, make it
	 * invalid.
	 */
	protected boolean notify;

	/**
	 * @param t See {@link #town}.
	 * @param trait See {@link #trait}.
	 * @param minimum See {@link #minimumrank}.
	 */
	public UrbanEvent(Town t,String trait,Rank minimum){
		town=t;
		this.trait=trait;
		minimumrank=minimum;
		notify=!town.ishostile();
	}

	@Override
	public boolean validate(Squad s,int squadel){
		if(trait!=null&&!town.traits.contains(trait)) return false;
		return super.validate(s,squadel)&&town.getrank().rank>=minimumrank.rank;
	}
}
