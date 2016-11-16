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
public abstract class Research implements Serializable {
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

	/** Constructor. */
	public Research(String name, String description, int cost) {
		this.name = name;
		this.description = description;
		this.cost = cost;
	}

	/**
	 * Put the card into play (it is discarded afterwards).
	 */
	abstract public void play(Town t);

	/**
	 * @return <code>false</code> if the current card makes no sense for the
	 *         given {@link Town}.
	 */
	abstract public boolean validate(Town t);

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
		Research r2 = (Research) obj;
		return name.equals(r2.name);
	}

	public static ArrayList<Research> get(Town t) {
		ArrayList<Research> options = new ArrayList<Research>(0);
		options.add(new Grow(t.size));
		options.add(new ResearchUpgrade());
		Collections.shuffle(options);
		return options;
	}
}
