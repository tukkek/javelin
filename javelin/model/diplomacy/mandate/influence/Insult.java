package javelin.model.diplomacy.mandate.influence;

import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;

/**
 * Degrades a {@link Relationship} with a faction, with the possiblity of
 * gaining favor with their enemies.
 *
 * @author alex
 */
public class Insult extends ImproveRelationship{
	/** Reflection constructor. */
	public Insult(Relationship r){
		super(r);
		targetbonus=-1;
		compatiblebonus=-1;
		incompatiblebonus=+1;
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.getstatus()>Relationship.HOSTILE;
	}

	@Override
	public String getname(){
		return "Insult "+target;
	}
}
