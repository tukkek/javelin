package javelin.controller.event.wild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.terrain.Desert;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.WildEvent;
import javelin.old.RPG;

/**
 * Events to be used with the {@link WildEvent} location.
 *
 * @author alex
 */
@SuppressWarnings("rawtypes")
public abstract class WildEventCard{
	static final List<Class> POSITIVE=new ArrayList<>(List.of());
	static final List<Class> NEUTRAL=new ArrayList<>(List.of());
	static final List<Class> NEGATIVE=new ArrayList<>(List.of());
	static final List<List<Class>> POOL=List.of(POSITIVE,NEUTRAL,NEGATIVE);

	/**
	 * Set to <code>false</code> to flag an event as non-instantaneous, meaning
	 * that it can be first defined but not "completed" right away, requiring a
	 * squad (same or another) to come back to resolve it. Note that at some point
	 * this must be flagged as <code>true</code> so that the {@link WildEvent} can
	 * be completed and removed from the {@link World}.
	 */
	public boolean remove=true;
	/** Used as {@link Actor}'s description after being visited. */
	public String name;

	public WildEventCard(String name){
		this.name=name;
	}

	/**
	 * @param s Usually {@link Squad#active}.
	 * @param el Encounter level of the given Squad.
	 * @param l The {@link WildEvent} Location.
	 *
	 * @return A valid, defined event ready to be used with equal chances of being
	 *         positive, neutral or negative.
	 */
	public static WildEventCard generate(Squad s,int el,WildEvent l){
		try{
			List<Class> type=RPG.pick(POOL);
			Collections.shuffle(type);
			for(Class c:type){
				WildEventCard card=(WildEventCard)c.getConstructor().newInstance();
				if(card.validate(s,l)){
					card.define(s,el,l);
					return card;
				}
			}
			throw new RuntimeException("Could not generate event!");
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Most cards don't require validation but for example, you may want to make
	 * sure the current {@link Location} is a {@link Desert} if this event only
	 * fits that particular climate. Note that certain events can be defined once
	 * and revisited later so mutable conditions such as time of day shouldn't be
	 * used for validation unless this is an instantaneous event.
	 *
	 * Parameters are the same as {@link #generate(Squad, int, Location)}.
	 *
	 * @return <code>true</code> if this particular card can be used.
	 */
	protected boolean validate(Squad s,WildEvent l){
		return true;
	}

	/**
	 * Events are ad-hoc (scaled to the Squad power level) and as such they are
	 * given this opportunity to configure themselves before being activated. This
	 * is called the first time a Squad enters a {@link WildEvent} location.
	 *
	 * Some events are not instantaneous and as such they may be defined once and
	 * activated many times later.
	 *
	 * Parameters are the same as {@link #generate(Squad, int, Location)}.
	 */
	public void define(Squad s,int el,WildEvent l){
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
	abstract public void happen(Squad s,WildEvent l);
}
