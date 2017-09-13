package javelin.model.unit.abilities.discipline.expertise;

import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;

/**
 * A discipline adapter for {@link CombatExpertiseManeuver}s.
 * 
 * @author alex
 */
public class CombatExpertiseDiscipline extends Discipline {
	public static final CombatExpertiseDiscipline INSTANCE = new CombatExpertiseDiscipline();

	private CombatExpertiseDiscipline() {
		super("Combat expertise");
		hasacademy = false;
	}

	@Override
	protected Maneuver[] getmaneuvers() {
		throw new UnsupportedOperationException();
	}
}
