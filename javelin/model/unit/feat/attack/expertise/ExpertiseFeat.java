package javelin.model.unit.feat.attack.expertise;

import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseDiscipline;
import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

public abstract class ExpertiseFeat extends Feat {
	public ExpertiseFeat(String namep) {
		super(namep);
	}

	@Override
	public boolean apply(Combatant c) {
		c.disciplines.add(CombatExpertiseDiscipline.INSTANCE, getmaneuver());
		return super.apply(c);
	}

	protected abstract CombatExpertiseManeuver getmaneuver();
}
