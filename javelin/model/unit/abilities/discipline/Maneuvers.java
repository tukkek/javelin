package javelin.model.unit.abilities.discipline;

import javelin.model.unit.CloneableList;

public class Maneuvers extends CloneableList<Maneuver> {
	public Maneuvers() {
		super();
	}

	public Maneuvers(Maneuvers m) {
		super(m);
	}

	@Override
	public Maneuvers clone() {
		return (Maneuvers) super.clone();
	}
}
