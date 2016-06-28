package javelin.model.unit.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

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
	public String load() {
		return " (" + Math.round(Squad.active.members.size() * 100 / capacity)
				+ "% load)";
	}

	/**
	 * @return the speed this convoy is moving on, considering the best way to
	 *         fit all given this vehicle's {@link #capacity}.
	 */
	public int getspeed(ArrayList<Combatant> tripulation) {
		if (tripulation.size() <= capacity) {
			return speed;
		}
		Combatant[] trailing = (Combatant[]) tripulation.stream()
				.filter(new Predicate<Combatant>() {
					@Override
					public boolean test(Combatant c) {
						return c.source.gettopspeed() < speed;
					}
				}).sorted(new Comparator<Combatant>() {
					@Override
					public int compare(Combatant o1, Combatant o2) {
						return o1.source.gettopspeed()
								- o2.source.gettopspeed();
					}
				}).toArray();
		return trailing.length > capacity
				? trailing[capacity].source.gettopspeed() : speed;
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
}