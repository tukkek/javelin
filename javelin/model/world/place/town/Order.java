package javelin.model.world.place.town;

import java.io.Serializable;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;

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
}
