package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.controller.terrain.Desert;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.World;

/**
 * A party that is lost will wander around and waste time.
 * 
 * @author alex
 */
public class GettingLost extends Hazard {
	int dc;

	/**
	 * @param dc
	 *            Survival check difficulty class.
	 */
	public GettingLost(int dc) {
		this.dc = dc;
	}

	@Override
	public boolean validate() {
		if (Squad.active.fly()) {
			return false;
		}
		if (!Squad.active.lastterrain.equals(Terrain.current())) {
			return false;
		}
		if (World.roads[Squad.active.x][Squad.active.y]
				&& Terrain.current().getweather() != Desert.SANDSTORM) {
			return false;
		}
		return Javelin.roll(Squad.active.survive()) < dc;
	}

	@Override
	public void hazard(int hoursellapsed) {
		getlost("Squad got lost!", hoursellapsed);
	}

	/**
	 * {@link Squad#displace()} on the active squad.
	 * 
	 * @param message
	 *            Shows this message in a prompt.
	 * @param hoursellapsed
	 *            This many more hours will be spent.
	 */
	public static void getlost(String message, int hoursellapsed) {
		Squad.active.displace();
		Squad.active.place();
		Squad.active.hourselapsed += hoursellapsed;
		Javelin.message(message, false);
	}
}
