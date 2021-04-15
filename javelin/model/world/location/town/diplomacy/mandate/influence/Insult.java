package javelin.model.world.location.town.diplomacy.mandate.influence;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;

/**
 * Degrades a {@link Relationship} with a faction, with the possiblity of
 * gaining favor with their enemies.
 *
 * @author alex
 */
public class Insult extends ImproveRelationship{
	/** Reflection constructor. */
	public Insult(Town t){
		super(t);
		targetbonus=-1;
		compatiblebonus=-1;
		incompatiblebonus=+1;
	}

	@Override
	public boolean validate(Diplomacy d){
		return d.town!=target&&target.diplomacy.getstatus()>-1;
	}

	@Override
	public String getname(){
		return "Insult "+target;
	}
}
