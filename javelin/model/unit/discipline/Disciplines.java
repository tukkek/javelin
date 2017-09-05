package javelin.model.unit.discipline;

import java.util.ArrayList;
import java.util.HashMap;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

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

	/**
	 * @param m
	 *            Given an item...
	 * @return an equal item in this instance. Mostly useful for finding the
	 *         same maneuver after its {@link Combatant} or {@link Monster} has
	 *         been cloned.
	 */
	public Maneuver find(Maneuver m) {
		for (Maneuvers discipline : values()) {
			int i = discipline.indexOf(m);
			if (i >= 0) {
				return discipline.get(i);
			}
		}
		return null;
	}

	@Override
	public Disciplines clone() {
		Disciplines disciplines = (Disciplines) super.clone();
		for (String discipline : disciplines.keySet()) {
			Maneuvers maneuvers = new Maneuvers(disciplines.get(discipline));
			for (int i = 0; i < maneuvers.size(); i++) {
				maneuvers.set(i, maneuvers.get(i).clone());
			}
			disciplines.put(discipline, maneuvers);
		}
		return disciplines;
	}
}
