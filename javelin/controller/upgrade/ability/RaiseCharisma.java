package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseCharisma extends RaiseAbility {

	public RaiseCharisma() {
		super("charisma");
	}

	@Override
			int getabilityvalue(Monster m) {
		return m.charisma;
	}

	@Override
			boolean setattribute(Combatant m, int l)
					throws PropertyVetoException {
		m.source.charisma = l;
		return true;
	}

}
