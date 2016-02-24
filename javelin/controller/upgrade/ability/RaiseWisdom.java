package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseWisdom extends RaiseAbility {
	public RaiseWisdom() {
		super("wisdom");
	}

	@Override
			int getabilityvalue(Monster m) {
		return m.wisdom;
	}

	@Override
			boolean setattribute(Combatant m, int l)
					throws PropertyVetoException {
		m.source.wisdom = l;
		m.source.raisewisdom();
		return true;
	}

}
