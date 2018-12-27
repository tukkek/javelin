package javelin.controller.event.urban;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ContentSummary;
import javelin.controller.event.EventCard;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

/**
 * @see UrbanEvents
 * @author alex
 */
public abstract class UrbanEvent extends EventCard{
	/**
	 * At least one element here must be in {@link Town#traits} for an event to be
	 * valid. If <code>null</code> or empty, will bypass this check.
	 *
	 * @see Town#traits
	 * @see #validate(Squad, int)
	 */
	protected List<String> traits;
	/** Minimum city size for this event to be valid. */
	protected Rank minimumrank;
	/** Town this event is happening in. */
	protected Town town;
	/**
	 * Alis for {@link Town#population} - also makes it easier to debug by
	 * overriding in a constructor.
	 *
	 * @see Javelin#DEBUG
	 */
	protected int el;

	/**
	 * @param t See {@link #town}. Should only be <code>null</code> during
	 *          {@link ContentSummary} analysis.
	 * @param traits See {@link #traits}.
	 * @param minimum See {@link #minimumrank}.
	 * @see UrbanEvents#printsummary(String)
	 */
	public UrbanEvent(Town t,List<String> traits,Rank minimum){
		town=t;
		el=town.population;
		this.traits=traits;
		minimumrank=minimum;
	}

	@Override
	public boolean validate(Squad s,int squadel){
		if(!super.validate(s,squadel)||town.getrank().rank<minimumrank.rank)
			return false;
		if(traits==null||traits.isEmpty()) return true;
		for(var t:town.traits)
			if(traits.contains(t)) return true;
		return false;
	}

	/**
	 * @param message Print this message to the player only as long as there is a
	 *          {@link Squad} in {@link #town} or if it isn't hostile.
	 * @see Town#ishostile()
	 */
	protected void notify(String message){
		if(town.notifyplayer()||town.getdistrict().getsquads().size()>0)
			Javelin.message(message,true);
	}
}
