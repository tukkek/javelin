package javelin.model.diplomacy.mandate;

import javelin.Javelin;
import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.unit.Alignment;
import javelin.model.world.location.town.Town;

/**
 * Reveals a {@link Town}'s {@link Alignment#ethics}.
 *
 * @author alex
 */
public class RevealAlignment extends Mandate{
	/** Reflection constructor. */
	public RevealAlignment(Town t){
		super(t);
	}

	@Override
	public boolean validate(Diplomacy d){
		return !target.diplomacy.showalignment;
	}

	@Override
	public String getname(){
		return "Reveal alignment for "+target;
	}

	@Override
	public void act(Diplomacy d){
		changealignment();
		String result="Alignment for "+target+" is: "
				+target.diplomacy.describealignment().toLowerCase()+".";
		Javelin.message(result,true);
	}

	/**
	 * Sets {@link Relationship#showalignment} or {@link Relationship#showmorals}.
	 */
	protected void changealignment(){
		target.diplomacy.showalignment=true;
	}
}
