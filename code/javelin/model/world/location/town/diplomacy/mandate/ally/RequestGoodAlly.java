/**
 *
 */
package javelin.model.world.location.town.diplomacy.mandate.ally;

import javelin.model.unit.Monster;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;

/** {@link Trait#RELIGIOUS} recruits. */
public class RequestGoodAlly extends RequestAlly{
	/** Constructor. */
	public RequestGoodAlly(Town t){
		super(t);
	}

	@Override
	protected boolean filter(Monster m){
		return !m.alignment.isevil();
	}
}
