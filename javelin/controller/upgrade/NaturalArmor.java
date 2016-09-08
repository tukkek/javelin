package javelin.controller.upgrade;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class NaturalArmor extends Upgrade {
	private final int target;

	public NaturalArmor(final String name, int target) {
		super(name);
		this.target = target;
	}

	@Override
	public String inform(final Combatant m) {
		return "Current armor class: " + m.source.ac;
	}

	@Override
	public boolean apply(final Combatant c) {
		Monster m = c.source;
		int newac = m.ac + target - m.armor;
		if (target < m.armor || newac > m.ac + 10
				|| newac > m.dexterity + m.constitution) {
			return false;
		}
		m.armor = target;
		m.ac = newac;
		return true;
	}
}