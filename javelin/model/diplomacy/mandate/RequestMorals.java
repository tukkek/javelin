package javelin.model.diplomacy.mandate;

import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.unit.Alignment;
import javelin.model.world.location.town.Town;

/**
 * Reveals a {@link Town}'s {@link Alignment#morals}.
 *
 * @see Relationship#showmorals
 * @author alex
 */
public class RequestMorals extends RequestEthics{
	/** Reflection constructor. */
	public RequestMorals(Relationship r){
		super(r);
	}

	@Override
	public boolean validate(Diplomacy d){
		return !target.showmorals;
	}

	@Override
	public String getname(){
		return "Reveal moral alignment for "+target;
	}

	@Override
	protected void changealignment(){
		target.showmorals=true;
	}
}
