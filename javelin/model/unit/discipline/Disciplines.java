package javelin.model.unit.discipline;

import java.util.ArrayList;
import java.util.HashMap;

public class Disciplines extends HashMap<String, Maneuvers> {
	public Disciplines() {
		super(0);
	}

	public Disciplines(Disciplines d) {
		super(d);
	}

	public void add(Discipline d, Maneuver m) {
		Maneuvers maneuvers = get(d.name);
		if (maneuvers == null) {
			maneuvers = new Maneuvers();
			put(d.name, maneuvers);
		}
		maneuvers.add(m);
	}

	public ArrayList<Maneuver> getmaneuvers() {
		ArrayList<Maneuver> maneuvers = new ArrayList<Maneuver>();
		for (Maneuvers discipline : values()) {
			maneuvers.addAll(discipline);
		}
		return maneuvers;
	}
}
