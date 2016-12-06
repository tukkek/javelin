package javelin.model.world.location.town.research;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import javelin.model.world.location.town.Town;

/**
 * Represents a card that can be used to progress a {@link Town}'s district.
 * 
 * @author alex
 */
public abstract class Labor implements Serializable {
	/** Card's name. */
	public String name;
	/** Short description of what it does. */
	public String description;
	/**
	 * Cost in labor.
	 * 
	 * @see Town#labor
	 */
	public int cost;
	public float progress;
	Town town;

	/** Constructor. */
	public Labor(String name, String description, int cost, Town t) {
		this.name = name;
		this.description = description;
		this.cost = cost;
		this.town = t;
	}

	/**
	 * Put the card into play (it is discarded afterwards).
	 */
	abstract public void done();

	/**
	 * @param d
	 * @return <code>false</code> if the current card makes no sense for the
	 *         given {@link Town}.
	 */
	abstract public boolean validate();

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

	public static ArrayList<Labor> get(Town t) {
		ArrayList<Labor> options = new ArrayList<Labor>(0);
		options.add(new Grow(t));
		// options.add(new ResearchUpgrade());
		Collections.shuffle(options);
		return options;
	}

	public void work(float step) {
		progress += step;
		if (progress > cost) {
			done();
		}
	}

	public String progress() {
		return Math.round(100 * progress / cost) + "%";
	}
}
