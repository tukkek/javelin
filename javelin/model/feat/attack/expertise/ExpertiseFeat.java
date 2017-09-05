package javelin.model.feat.attack.expertise;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.discipline.expertise.CombatExpertiseDiscipline;
import javelin.model.unit.discipline.expertise.CombatExpertiseManeuver;

public abstract class ExpertiseFeat extends Feat {
	public ExpertiseFeat(String namep) {
		super(namep);
	}

	@Override
	public boolean apply(Combatant c) {
		c.source.disciplines.add(CombatExpertiseDiscipline.INSTANCE, getmaneuver());
		return super.apply(c);
	}

	protected abstract CombatExpertiseManeuver getmaneuver();
}
