package javelin.controller.upgrade.damage;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Upgrades damage.
 */
public abstract class Damage extends Upgrade {
	public Damage(final String name) {
		super(name);
	}

	protected abstract List<AttackSequence> getattacktype(final Monster m);

	@Override
	public String info(final Combatant m) {
		List<String> information = new ArrayList<String>();
		information.add("Current: ");
		for (final List<Attack> sequence : getattacktype(m.source)) {
			for (final Attack a : sequence) {
				String damage = a.formatDamage() + ", ";
				if (!information.contains(damage)) {
					information.add(damage);
				}
			}
		}
		String out = "";
		for (String s : information) {
			out += s;
		}
		return out.substring(0, out.length() - 2);
	}

	@Override
	public boolean apply(final Combatant m) {
		final List<List<Attack>> all =
				new ArrayList<List<Attack>>(getattacktype(m.source));
		if (all.isEmpty()) {
			return false;
		}
		for (final List<Attack> sequence : all) {
			for (final Attack a : sequence) {
				a.damage[0] += 1;
			}
		}
		incrementupgradecount(m.source);
		return true;
	}

	public abstract void incrementupgradecount(Monster m);

	@Override
	public boolean isstackable() {
		return false;
	}
}