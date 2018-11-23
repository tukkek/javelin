package javelin.controller.event.wild;

import javelin.controller.event.EventCard;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.PointOfInterest;

/**
 * @see PointOfInterest
 * @see WildEvents
 *
 * @author alex
 */
public abstract class WildEvent extends EventCard{
	/** Used as the {@link PointOfInterest}'s description after being visited. */
	public String name;

	/**
	 * Set to <code>false</code> to flag an event as non-instantaneous, meaning
	 * that it can be first defined but not "completed" right away, requiring a
	 * squad (same or another) to come back to resolve it. Note that at some point
	 * this must be flagged as <code>true</code> so that the {@link WildEvent} can
	 * be completed and removed from the {@link World}.
	 *
	 * If this is <code>false</code>,
	 * {@link #happen(Squad, javelin.model.world.location.PointOfInterest)} might
	 * be called more than once for the same instance of this card.
	 */
	public boolean remove=true;

	/**
	 * Creates a named card. Subclasses should have zero-argument,
	 * reflection-friendly constructors.
	 */
	public WildEvent(String name){
		this.name=name;
	}
}
