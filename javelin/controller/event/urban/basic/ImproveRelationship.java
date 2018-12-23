package javelin.controller.event.urban.basic;

import javelin.Javelin;
import javelin.controller.event.urban.UrbanEvent;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;

/**
 * Boosts a {@link Relationship} by 1.
 *
 * @author alex
 */
public class ImproveRelationship extends UrbanEvent{
	/**
	 * Will invalidate if current {@link Relationship} status sits at this level.
	 */
	protected int forbid=Relationship.ALLY;
	/**
	 * How much to change the relationship for.
	 *
	 * @see Relationship#changestatus(int)
	 */
	protected int change=+1;
	/** Representation of what is happening to the relationship status. */
	protected String description="improves";

	Relationship relationship;

	/** Reflection constructor. */
	public ImproveRelationship(Town t){
		super(t,null,Rank.HAMLET);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		var d=Diplomacy.instance;
		if(d==null) return false;
		relationship=d.getdiscovered().get(town);
		if(relationship==null||relationship.getstatus()==forbid) return false;
		return super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		relationship.changestatus(change);
		if(notify){
			var status=relationship.describestatus().toLowerCase();
			Javelin.message("Relationship with "+town+" spontaneously "+description
					+" to: "+status+"!",true);
		}
	}
}
