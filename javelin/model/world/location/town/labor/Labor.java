package javelin.model.world.location.town.labor;

import java.io.Serializable;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
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
		this.minimumrank = minimumsize;
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

	public void work(float step) {
		progress += step;
		if (progress >= cost) {
			done();
			cancel();
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
		town.governor.addproject(this);
		town.governor.removefromhand(this);
		work(0);
	}

	public void cancel() {
		town.governor.removeproject(this);
	}

	public void discard() {
		town.governor.removefromhand(this);
	}
}
