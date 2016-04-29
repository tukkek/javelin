package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseIntelligence extends RaiseAbility {
	public RaiseIntelligence() {
		super("intelligence");
	}

	@Override
			int getabilityvalue(Monster m) {
		return m.intelligence;
	}

	@Override
			boolean setattribute(Combatant m, int l)
					throws PropertyVetoException {
		m.source.raiseintelligence(+2);
		return true;
	}

	@Override
	public boolean apply(Combatant m) {
		return m.source.intelligence != 0 && super.apply(m);
	}
}
