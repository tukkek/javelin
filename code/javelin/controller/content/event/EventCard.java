package javelin.controller.content.event;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.content.fight.Fight;
import javelin.controller.content.terrain.Desert;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.location.Location;
import javelin.model.world.location.PointOfInterest;

/**
 * Events to be used with the {@link PointOfInterest} location.
 *
 * @author alex
 */
public abstract class EventCard{
	/**
	 * A fight to be triggered from an {@link EventCard}.
	 *
	 * @author alex
	 */
	protected class EventFight extends Fight{
		ArrayList<Combatant> foes=new ArrayList<>(1);

		/** Constructor. */
		protected EventFight(Actor l){
			terrains=List.of(Terrain.get(l.x,l.y));
			hide=false;
			bribe=false;
		}

		/** Constructor. */
		public EventFight(Combatant mercenary,Actor l){
			this(l);
			foes.add(mercenary);
		}

		/** Constructor. */
		public EventFight(List<Combatant> foes,Actor l){
			this(l);
			this.foes.addAll(foes);
		}

		@Override
		public ArrayList<Combatant> getfoes(Integer teamel){
			return foes;
		}
	}

	/**
	 * Most cards don't require validation but for example, you may want to make
	 * sure the current {@link Location} is a {@link Desert} if this event only
	 * fits that particular climate. Note that certain events can be defined once
	 * and revisited later so mutable conditions such as time of day shouldn't be
	 * used for validation unless this is an instantaneous event.
	 *
	 * This is called before {@link #define(Squad, int)}.
	 *
	 * Parameters are the same as
	 * {@link EventDealer#generate(Squad, int, Location)}.
	 *
	 * @param squad A relevant squad to the event or <code>null</code>.
	 *
	 * @return <code>true</code> if this particular card can be used.
	 */
	public boolean validate(Squad s,int squadel){
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
	 * This is called after {@link #validate(Squad, int)} and is meant to contain
	 * any slower operations, as it's only called once when a valid card is
	 * selected.
	 *
	 * @param squad A relevant squad to the event or <code>null</code>.
	 *
	 *          Parameters are the same as
	 *          {@link EventDealer#generate(Squad, int, Location)}.
	 */
	public void define(Squad s,int squadel){
		return;
	}

	/**
	 * Activates this event, after validation and definition.
	 *
	 * @param squad A relevant squad to the event or <code>null</code>.
	 */
	abstract public void happen(Squad s);
}
