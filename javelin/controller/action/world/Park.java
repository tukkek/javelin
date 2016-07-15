package javelin.controller.action.world;

import com.sun.glass.events.KeyEvent;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.ParkedVehicle;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.view.screen.WorldScreen;

/**
 * Park and exit your current vehicle.
 * 
 * @author alex
 */
public class Park extends WorldAction {

	public Park() {
		super("Park your vehicle", new int[] { KeyEvent.VK_P },
				new String[] { "p" });
	}

	@Override
	public void perform(WorldScreen screen) {
		if (Squad.active.transport == null) {
			throw new RepeatTurn();
		}
		if (!Squad.active.transport.parkeable) {
			Javelin.message("This vehicle cannot sustain itself on it's own...",
					false);
			throw new RepeatTurn();
		}
		Squad s = Squad.active;
		Point exit = null;
		for (int x = s.x - 1; x <= s.x + 1; x++) {
			for (int y = s.y - 1; y <= s.y + 1; y++) {
				if ((x == s.x && y == s.y) || !World.validatecoordinate(x, y)
						|| Terrain.get(x, y).equals(Terrain.WATER)
						|| WorldActor.get(x, y) != null) {
					continue;
				}
				exit = new Point(x, y);
				break;
			}
		}
		if (exit == null) {
			Javelin.message(
					"You need to be close to a free land space to park your vehicle...",
					false);
			throw new RepeatTurn();
		}
		int parkingx = Squad.active.x;
		int parkingy = Squad.active.y;
		Squad.active.visual.remove();
		Squad.active.move(exit.x, exit.y);
		Squad.active.place();
		new ParkedVehicle(parkingx, parkingy, Squad.active.transport).place();
		Squad.active.transport = null;
		Squad.active.updateavatar();
	}
}
