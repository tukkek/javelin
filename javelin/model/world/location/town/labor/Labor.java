package javelin.model.world.location.town.labor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

/**
 * Represents a card that can be used to progress a {@link Town}'s district.
 * 
 * @author alex
 */
public abstract class Labor implements Serializable {
	public static ArrayList<Labor> get(Town t) {
		ArrayList<Labor> options = new ArrayList<Labor>(0);
		options.add(new Grow(t));
		options.add(new Settler(t));
		Collections.shuffle(options);
		return options;
	}

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
	Town town;

	/** Constructor. */
	public Labor(String name, int cost, Town t) {
		this.name = name;
		this.cost = cost;
		this.town = t;
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
	abstract public boolean validate(District d);

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		Labor r2 = (Labor) obj;
		return name.equals(r2.name);
	}

	public void work(float step) {
		progress += step;
		if (progress >= cost) {
			done();
			town.governor.removefromqueue(this);
		}
	}

	public String progress() {
		return Math.round(100 * progress / cost) + "%";
	}
}
