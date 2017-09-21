package javelin.model.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.ParkedVehicle;
import javelin.model.world.World;

/**
 * Vehicles improve speed / random encounter chance.
 *
 * @see Squad#transport
 */
public class Transport implements Serializable {
	/** See {@link Carriage}. */
	public static final Transport CARRIAGE = new Carriage();
	/** See {@link Ship}. */
	public static final Transport SHIP = new Ship();
	/** See {@link Airship}. */
	public static final Transport AIRSHIP = new Airship();

	/** Transport description. */
	public String name;
	/** Movement speed. */
	public int speed;
	/** Maximum number of people on board. */
	public int capacity;
	/** Daily upkeep. */
	public int maintenance;
	/** Price for researching. */
	public int research;
	/** Price of a new transport. */
	public int price;
	/** <code>true</code> if able to move on water for any reason. */
	public boolean sails = false;
	/** <code>true</code> if flies and ignores terrain. */
	public boolean flies = false;
	/**
	 * <code>true</code> if the vehicle crew can sustain itself while away from
	 * a {@link Squad}.
	 */
	public boolean parkeable = true;

	Transport(String namep, int speedp, int capacityp, int maintenancep,
			int price, int researchcost) {
		name = namep;
		speed = speedp;
		capacity = capacityp;
		maintenance = maintenancep;
		this.price = price;
		research = researchcost;
	}

	/**
	 * @return Description of current capacity load.
	 */
	public String load(ArrayList<Combatant> tripulation) {
		ArrayList<Combatant> trailing = gettrailing(tripulation);
		return checkload(trailing) ? " ("
				+ Math.round(trailing.size() * 100 / capacity) + "% load)"
				: " (overloaded)";
	}

	/**
	 * @return the speed this convoy is moving on, considering the best way to
	 *         fit all given this vehicle's {@link #capacity}.
	 */
	public int getspeed(ArrayList<Combatant> tripulation) {
		if (tripulation.size() <= capacity) {
			return speed;
		}
		ArrayList<Combatant> trailing = gettrailing(tripulation);
		return trailing.size() > capacity
				? Math.round(trailing.get(capacity).source.gettopspeed()
						* WorldMove.NORMALMARCH)
				: speed;
	}

	ArrayList<Combatant> gettrailing(ArrayList<Combatant> tripulation) {
		ArrayList<Combatant> trailing = new ArrayList<Combatant>(tripulation);
		for (Combatant c : tripulation) {
			if (c.source.gettopspeed() * WorldMove.NORMALMARCH >= speed) {
				trailing.remove(c);
			}
		}
		trailing.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o1.source.gettopspeed() - o2.source.gettopspeed();
			}
		});
		return trailing;
	}

	/**
	 * @return <code>false</code> if overloaded (transport cannot carry all
	 *         units).
	 * @see #getspeed(ArrayList)
	 */
	public boolean checkload(ArrayList<Combatant> tripulation) {
		return getspeed(tripulation) == speed;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return <code>true</code> if can start a fight while moving in this
	 *         vehicle.
	 */
	public boolean battle() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && name.equals(((Transport) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Pays the {@link #maintenance} upkeep and destroys the transport
	 * otherwise. Will also sink with tripulation that can't swim.
	 *
	 * @see Squad#transport
	 */
	public void keep(Squad s) {
		s.gold -= maintenance;
		if (s.gold < 0) {
			s.gold = 0;
			s.transport = null;
			s.updateavatar();
			if (Terrain.get(s.x, s.y).equals(Terrain.WATER)) {
				String message = "The " + toString()
						+ " sinks, taking all non-swimmers with it!";
				Javelin.message(message, true);
				for (Combatant c : s.members) {
					if (c.source.swim() == 0) {
						s.remove(c);
					}
				}
			}
		}
	}

	/**
	 * @throws RepeatTurn
	 *             If can't park - with a message explaining why.
	 */
	public void park() {
		if (!parkeable) {
			throw new RepeatTurn(
					"This vehicle cannot sustain itself on its own...");
		}
		Squad s = Squad.active;
		Point exit = null;
		ArrayList<Actor> getactors = World.getactors();
		for (int x = s.x - 1; x <= s.x + 1; x++) {
			for (int y = s.y - 1; y <= s.y + 1; y++) {
				if (x == s.x && y == s.y || !World.validatecoordinate(x, y)
						|| Terrain.get(x, y).equals(Terrain.WATER)
						|| World.get(x, y, getactors) != null) {
					continue;
				}
				exit = new Point(x, y);
				break;
			}
		}
		if (exit == null) {
			throw new RepeatTurn(
					"You need to be close to a free land space to park your vehicle...");
		}
		int x = s.x;
		int y = s.y;
		s.move(exit.x, exit.y);
		s.place();
		new ParkedVehicle(x, y, this).place();
		s.transport = null;
		s.updateavatar();
	}
}