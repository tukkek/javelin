/**
 *
 */
package javelin.model.world.location.town.diplomacy.mandate.ally;

import javelin.model.unit.Monster;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;

/** {@link Trait#MILITARY} recruits. */
public class RequestLawfulAlly extends RequestAlly{
	/** Constructor. */
	public RequestLawfulAlly(Town t){
		super(t);
	}

	@Override
	protected boolean filter(Monster m){
		return !m.alignment.ischaotic();
	}
}
