package javelin.model.unit.abilities.discipline.expertise;

import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;

/**
 * A discipline adapter for {@link CombatExpertiseManeuver}s.
 *
 * TODO would be good to completely give away with the concept of "Virtual
 * disciplines" like this but right now {@link Maneuver}s need to be tied to a
 * {@link Discipline}.
 *
 * @author alex
 */
public class CombatExpertiseDiscipline extends Discipline{
	public static final CombatExpertiseDiscipline INSTANCE=new CombatExpertiseDiscipline();

	private CombatExpertiseDiscipline(){
		super("Combat expertise");
	}

	@Override
	protected Maneuver[] getmaneuvers(){
		throw new UnsupportedOperationException();
	}
}
