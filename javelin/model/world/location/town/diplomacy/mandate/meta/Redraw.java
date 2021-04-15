package javelin.model.world.location.town.diplomacy.mandate.meta;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.model.world.location.town.diplomacy.mandate.Mandate;

/**
 * Clears {@link Diplomacy#treaties}.
 *
 * @author alex
 */
public class Redraw extends Mandate{
	/** Reflection constructor. */
	public Redraw(Town t){
		super(t);
	}

	@Override
	public String getname(){
		return "Discard all current treaties";
	}

	@Override
	public boolean validate(Diplomacy d){
		return !d.treaties.isEmpty();
	}

	@Override
	public void act(Diplomacy d){
		d.treaties.clear();
	}
}
