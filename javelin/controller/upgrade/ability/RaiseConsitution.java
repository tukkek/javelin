package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseConsitution extends RaiseAbility {

	public RaiseConsitution() {
		super("constitution");
	}

	@Override
			int getabilityvalue(Monster m) {
		return m.constitution;
	}

	@Override
			boolean setattribute(Combatant m, int l)
					throws PropertyVetoException {
		m.source.constitution = l;
		m.source.raiseconstitution(m);
		return true;
	}

}
