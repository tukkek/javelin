package javelin.controller.event;

import javelin.controller.terrain.Desert;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.PointOfInterest;

/**
 * Events to be used with the {@link PointOfInterest} location.
 *
 * @author alex
 */
@SuppressWarnings("rawtypes")
public abstract class EventCard{
	/**
	 * Most cards don't require validation but for example, you may want to make
	 * sure the current {@link Location} is a {@link Desert} if this event only
	 * fits that particular climate. Note that certain events can be defined once
	 * and revisited later so mutable conditions such as time of day shouldn't be
	 * used for validation unless this is an instantaneous event.
	 *
	 * This is called before {@link #define(Squad, int, PointOfInterest)}.
	 *
	 * Parameters are the same as
	 * {@link EventDealer#generate(Squad, int, Location)}.
	 *
	 * @return <code>true</code> if this particular card can be used.
	 */
	@SuppressWarnings("static-method")
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		return true;
	}

	/**
	 * Events are ad-hoc (scaled to the Squad power level) and as such they are
	 * given this opportunity to configure themselves before being activated. This
	 * is called the first time a Squad enters a {@link PointOfInterest} location.
	 *
	 * Some events are not instantaneous and as such they may be defined once and
	 * activated many times later.
	 *
	 * This is called after {@link #validate(Squad, int, PointOfInterest)} and is
	 * meant to contain any slower operations, as it's only called once when a
	 * valid card is selected.
	 *
	 * Parameters are the same as
	 * {@link EventDealer#generate(Squad, int, Location)}.
	 */
	@SuppressWarnings("static-method")
	public void define(Squad s,int squadel,PointOfInterest l){
		return;
	}

	/**
	 * Activates this event, after validation and definition.
	 *
	 * @param s Usually {@link Squad#active}. Note that this might not be the same
	 *          Squad as the one used to validate and define this card.
	 * @param l The location in question. Always the same one used for validation
	 *          and definition. Need not be removed manually, use {@link #remove}
	 *          instead.
	 */
	abstract public void happen(Squad s,PointOfInterest l);
}
