package javelin.model.unit.abilities.discipline;

import javelin.model.unit.CloneableList;

/**
 * Note that the natural ordering of {@link Maneuver}s are from highest level to
 * lowest level.
 * 
 * @author alex
 */
public class Maneuvers extends CloneableList<Maneuver> {
	public Maneuvers() {
		super();
	}

	public Maneuvers(Maneuvers m) {
		super(m);
	}

	public Maneuvers(int size) {
		super(size);
	}

	@Override
	public Maneuvers clone() {
		return (Maneuvers) super.clone();
	}

	public void sort() {
		super.sort(null);
	}
}
