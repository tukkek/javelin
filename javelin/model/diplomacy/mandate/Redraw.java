package javelin.model.diplomacy.mandate;

import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;

/**
 * Clears {@link Diplomacy#hand}.
 *
 * @author alex
 */
public class Redraw extends Mandate{
	/** Reflection constructor. */
	public Redraw(Relationship r){
		super(r);
	}

	@Override
	public String getname(){
		return "Discard all diplomatic actions and redraw";
	}

	@Override
	public boolean validate(Diplomacy d){
		return !d.hand.isEmpty();
	}

	@Override
	public void act(Diplomacy d){
		d.hand.clear();
	}
}
