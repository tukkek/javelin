package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises one of the six attributes.
 * 
 * @author alex
 */
public abstract class RaiseAbility extends Upgrade {
	private final String abilityname;

	public RaiseAbility(String abilityname) {
		super("Raise ability: " + abilityname);
		this.abilityname = abilityname;
	}

	@Override
	public String info(Combatant m) {
		String out = "Current " + abilityname + ": ";
		int bonus = Monster.getbonus(getabilityvalue(m.source));
		if (bonus > 0) {
			out += "+";
		}
		return out + bonus;
	}

	abstract int getabilityvalue(Monster m);

	@Override
	public boolean apply(Combatant m) {
		try {
			return setattribute(m, getabilityvalue(m.source) + 2);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
	}

	abstract boolean setattribute(Combatant m, int l)
			throws PropertyVetoException;

	@Override
	public boolean isstackable() {
		return false;
	}

}
