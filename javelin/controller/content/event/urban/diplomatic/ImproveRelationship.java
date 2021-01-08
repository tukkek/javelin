package javelin.controller.content.event.urban.diplomatic;

import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

/**
 * Boosts a {@link Relationship} by 1.
 *
 * @author alex
 */
public class ImproveRelationship extends DiplomaticEvent{
	/**
	 * How much to change the relationship for.
	 *
	 * @see Relationship#changestatus(int)
	 */
	protected int change=+1;
	/** Representation of what is happening to the relationship status. */
	protected String description="improves";

	Town relationship;

	/** Reflection constructor. */
	public ImproveRelationship(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		relationship=town;
		if(relationship==null) return false;
		return super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		relationship.diplomacy.reputation+=town.population*change;
		var status=relationship.diplomacy.describestatus().toLowerCase();
		notify("Relationship with "+town+" spontaneously "+description+" to: "
				+status+"!");
	}
}
