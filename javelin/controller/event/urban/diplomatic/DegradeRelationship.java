package javelin.controller.event.urban.diplomatic;

import javelin.model.world.location.town.Town;

/**
 * Spontaneously lowers a {@link Relationship}.
 *
 * @author alex
 */
public class DegradeRelationship extends ImproveRelationship{
	/** Reflection constructor. */
	public DegradeRelationship(Town t){
		super(t);
		change=-1;
		description="degrades";
	}
}
