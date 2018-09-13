package javelin.model.world.location.town.labor;

import java.io.Serializable;

import javelin.Javelin;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.governor.Governor;
import javelin.view.screen.WorldScreen;

/**
 * Represents a card that can be used to progress a {@link Town}'s district.
 * Initially each labor is an empty placeholder inside a {@link Deck}. Upon
 * drawing they are cloned and the clones defined as actual cards via
 * {@link #define(Town)}.
 *
 * The suggested value for {@link #cost} is 5/10/15/20 for
 * weak/medium/powerful/epic labors.
 *
 * @author alex
 */
public abstract class Labor implements Serializable, Cloneable {
	/** Card's name. */
	public String name;
	/**
	 * Cost in labor.
	 *
	 * @see Town#labor
	 */
	public int cost;
	public float progress;
	public boolean automatic = true;
	public Town town;
	/** <code>true</code> to return to {@link WorldScreen} after selection. */
	public boolean closescreen = false;
	public boolean construction = false;
	protected Rank minimumrank;

	/**
	 * Define here all the data that isn't {@link Town}-dependent.
	 *
	 * @param name
	 *            For debug purposes a labor, even in its original abstract mode
	 *            must define a name for {@link #toString()}. This can updated
	 *            later on in {@link #define()}.
	 */
	public Labor(String name, int cost, Rank minimumsize) {
		this.name = name;
		this.cost = cost;
		minimumrank = minimumsize;
	}

	public Labor generate(Town t) {
		Labor clone = clone();
		clone.town = t;
		clone.define();
		return clone;
	}

	/**
	 * Redefines the card date now that this newly cloned instance has been
	 * associated with a specific {@link #town}.
	 */
	abstract protected void define();

	@Override
	protected Labor clone() {
		try {
			return (Labor) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
	}

	/**
	 * Put the card into play (it is discarded afterwards).
	 */
	abstract public void done();

	/**
	 * @param d
	 *            This is used as a cache, see {@link District} for more
	 *            details.
	 * @return <code>false</code> if the current card makes no sense for the
	 *         given {@link Town}.
	 */
	public boolean validate(District d) {
		return d.town.getrank().rank >= minimumrank.rank;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass());
	}

	/**
	 * This is only after all labors from a {@link Governor} are ensured to be
	 * valid as in {@link #validate(District)}.
	 *
	 * @param step
	 *            Works this amount of daily Labor in order to progress this
	 *            project.
	 *
	 * @see Town#DAILYLABOR
	 */
	public void work(float step) {
		progress += step;
		if (progress >= cost) {
			ready();
		}
	}

	/** Calls {@link #done()} and cleans up. */
	protected void ready() {
		done();
		cancel();
		if (this.cost != 0 && !town.ishostile()
				&& WorldScreen.current != null) {
			WorldScreen.current.center(town.x, town.y);
			Javelin.redraw();
			Javelin.message(name + ", finished on " + town + "!", true);
		}
	}

	/**
	 * @return A percentage value from 0 to 100.
	 */
	public int getprogress() {
		return Math.round(100 * progress / cost);
	}

	/**
	 * Starts work on this labor. Usually a upgrade doesn't have any effect
	 * until {@link #done()} but this can overriden if necessary.
	 */
	public void start() {
		town.getgovernor().addproject(this);
		town.getgovernor().removefromhand(this);
		work(0);
	}

	public void cancel() {
		town.getgovernor().removeproject(this);
	}

	public void discard() {
		town.getgovernor().removefromhand(this);
	}
}
