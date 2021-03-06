package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Carriage;
import javelin.model.unit.transport.Transport;
import javelin.model.world.World;

/**
 * {@link Carriage}s are fragile vehicles and prone to breaking.
 * 
 * @author alex
 */
public class Break extends Hazard {

	@Override
	public void hazard(int hoursellapsed) {
		Javelin.message("The rough terrain damages your carriage!", true);
		Squad.active.transport = null;
		Squad.active.updateavatar();
	}

	@Override
	public boolean validate() {
		return Transport.CARRIAGE.equals(Squad.active.transport)
				&& !World.roads[Squad.active.x][Squad.active.y];
	}
}
