package javelin.controller.upgrade.feat;

import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import javelin.model.feat.WeaponFocus;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see WeaponFocus
 * @author alex
 */
public class MeleeFocus extends FeatUpgrade {
	public static TreeMap<String, Double> bab = new TreeMap<String, Double>();

	static {
		bab.put("aberration", 3.0 / 4.0);
		bab.put("animal", 3.0 / 4.0);
		bab.put("construct", 3.0 / 4.0);
		bab.put("dragon", 1.0);
		bab.put("elemental", 3.0 / 4.0);
		bab.put("fey", 1.0 / 2.0);
		bab.put("giant", 3.0 / 4.0);
		bab.put("humanoid", 3.0 / 4.0);
		bab.put("magical beast", 1.0);
		bab.put("monstrous humanoid", 1.0);
		bab.put("ooze", 3.0 / 4.0);
		bab.put("outsider", 1.0);
		bab.put("plant", 3.0 / 4.0);
		bab.put("undead", 1.0 / 2.0);
		bab.put("vermin", 3.0 / 4.0);
	}

	public MeleeFocus(final String name) {
		super(name, javelin.model.feat.WeaponFocus.singleton);
	}

	@Override
	public String info(final Combatant m) {
		return "Base attack bonus: " + m.source.getbaseattackbonus();
	}

	@Override
	public boolean isstackable() {
		return false;
	}

	// @Override
	// public Double crcost(final Monster m) {
	// return ? null : super
	// .crcost(m) * countattacks(m);
	// }

	private int countattacks(final Monster m) {
		final HashSet<String> attacks = new HashSet<String>();
		for (final List<Attack> as : getattacks(m)) {
			for (final Attack a : as) {
				attacks.add(a.name);
			}
		}
		return attacks.size();
	}

	protected List<AttackSequence> getattacks(final Monster m) {
		return m.melee;
	}

	@Override
	public boolean apply(final Combatant c) {
		Monster m = c.source;
		if (m.hasfeat(javelin.model.feat.WeaponFocus.singleton)
				|| getattacks(m).isEmpty() || m.getbaseattackbonus() < 1) {
			return false;
		}
		for (int i = 0; i < countattacks(m); i++) {
			super.apply(c);
		}
		for (final List<Attack> as : getattacks(m)) {
			for (final Attack a : as) {
				a.bonus += 1;
			}
		}
		return true;
	}
}
