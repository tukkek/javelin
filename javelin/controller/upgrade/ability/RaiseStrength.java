package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class RaiseStrength extends RaiseAbility {
	public RaiseStrength() {
		super("strength");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.strength;
	}

	@Override
	boolean setattribute(Combatant m, int i) throws PropertyVetoException {
		if (!m.source.raisestrength()) {
			return false;
		}
		m.source.strength = i;
		return true;
	}

}
