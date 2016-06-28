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
	public String info(final Combatant m) {
		return "Current armor class: " + m.source.ac;
	}

	@Override
	public boolean apply(final Combatant c) {
		Monster m = c.source;
		if (m.armor >= target) {
			return false;
		}
		int delta = target - m.armor;
		m.armor = target;
		m.ac += delta;
		return true;
	}
}