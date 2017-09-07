package javelin.model.world.location.order;

import java.io.Serializable;

import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;

/**
 * Represents a training {@link Combatant} or a forging {@link Item}.
 * 
 * @author alex
 */
public class Order implements Serializable {
	/**
	 * The time at which this order will be ready.
	 * 
	 * @see Squad#hourselapsed
	 */
	public long completionat;
	/** Description. */
	public String name;

	/**
	 * @param eta
	 *            Total amount of time this order will need to be ready,
	 *            starting from now.
	 * @param namep
	 *            See {@link #name}.
	 */
	public Order(long eta, String namep) {
		this.completionat = eta + Squad.active.hourselapsed;
		this.name = namep;
	}

	/**
	 * @param time
	 *            Given the current time...
	 * @return if this order is ready or not.
	 */
	public boolean completed(long time) {
		return completionat <= time;
	}

	@Override
	public String toString() {
		return name + " (" + geteta(Squad.active.hourselapsed) + ")";
	}

	public String geteta(long now) {
		long hoursleft = Math.max(0, completionat - now);
		if (hoursleft == 0) {
			return "ready";
		}
		return hoursleft >= 24 ? Math.round(hoursleft / 24f) + " days"
				: hoursleft + " hours";
	}
}
