package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.feat.WeaponFinesse;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseStrength extends RaiseAbility {
	public RaiseStrength() {
		super("strength");
	}

	@Override
			int getabilityvalue(Monster m) {
		return m.strength;
	}

	@Override
			boolean setattribute(Combatant m, int i)
					throws PropertyVetoException {
		if (m.source.hasfeat(WeaponFinesse.singleton)) {
			return false;
		}
		m.source.raisestrength(+1);
		return true;
	}

}
