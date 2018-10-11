package javelin.controller.fight;

import javelin.controller.Weather;
import javelin.controller.map.Arena;
import javelin.model.world.location.Location;
import javelin.model.world.location.unique.TrainingHall;

/**
 * A {@link Fight} that happens inside the {@link TrainingHall}.
 *
 * @author alex
 */
public class TrainingSession extends Siege{
	TrainingHall hall;

	/** See {@link Siege#Siege(Location)}. */
	public TrainingSession(TrainingHall hall){
		super(hall);
		this.hall=hall;
		friendly=true;
		rewardgold=false;
		bribe=false;
		hide=false;
		cleargarrison=false;
		map=new Arena();
		map.maxflooding=Weather.DRY;
	}

	@Override
	public boolean onend(){
		super.onend();
		if(Fight.victory) hall.level();
		return true;
	}
}
