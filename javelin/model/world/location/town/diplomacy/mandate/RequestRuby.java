/**
 *
 */
package javelin.model.world.location.town.diplomacy.mandate;

import javelin.model.item.consumable.Ruby;
import javelin.model.world.location.town.Town;

/** @see Ruby */
public class RequestRuby extends Mandate{
	/** Constructor. */
	public RequestRuby(Town t){
		super(t);
	}

	@Override
	public String getname(){
		return "Request ruby";
	}

	@Override
	public boolean validate(){
		return true;
	}

	@Override
	public void act(){
		new Ruby().grab();
	}
}
