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
		return super.apply(c) && c
				.addmaneuver(CombatExpertiseDiscipline.INSTANCE, getmaneuver());
	}

	protected abstract CombatExpertiseManeuver getmaneuver();
}
