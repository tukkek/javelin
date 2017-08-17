package javelin.model.world;

import java.awt.Image;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.Park;
import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.view.Images;

/**
 * Represents a {@link Park}ed {@link Transport}.
 *
 * @see Squad#transport
 * @see Transport#parkeable
 * @author alex
 */
public class ParkedVehicle extends Actor {
	public Transport transport;

	/**
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @param transport
	 *            Vehicle to be parked.
	 */
	public ParkedVehicle(int x, int y, Transport transport) {
		this.x = x;
		this.y = y;
		this.transport = transport;
	}

	@Override
	public boolean interact() {
		if (Squad.active.transport != null) {
			if (Javelin.prompt(
					"Are you sure you want to abandon your current vehicle? Press c to confirm....") != 'c') {
				throw new RepeatTurn();
			}
		}
		Squad.active.transport = transport;
		remove();
		Squad.active.move(x, y);
		Squad.active.place();
		Squad.active.updateavatar();
		return true;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return true;
	}

	@Override
	public Image getimage() {
		return Images.getImage(transport.name.toLowerCase());
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	public String describe() {
		return "Parked " + transport.name.toLowerCase() + ".";
	}
}
