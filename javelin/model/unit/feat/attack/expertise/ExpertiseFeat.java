package javelin.model.unit.feat.attack.expertise;

import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseDiscipline;
import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

public abstract class ExpertiseFeat extends Feat {
	public ExpertiseFeat(String namep) {
		super(namep);
		update = true;
	}

	@Override
	public boolean upgrade(Combatant c) {
		return super.upgrade(c) && add(c);
	}

	protected abstract CombatExpertiseManeuver getmaneuver();

	@Override
	public boolean add(Combatant c) {
		return c.addmaneuver(CombatExpertiseDiscipline.INSTANCE, getmaneuver());
	}
}
