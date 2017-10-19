package javelin.model.unit.feat.attack.focus;

import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.db.reader.fields.Feats;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class WeaponFocus extends Feat {
	/**
	 * Map of base attack bonus increment per level by lower-case monster type.
	 */
	public static final TreeMap<String, Double> BAB =
			new TreeMap<String, Double>();
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new WeaponFocus();

	static {
		BAB.put("aberration", 3.0 / 4.0);
		BAB.put("animal", 3.0 / 4.0);
		BAB.put("construct", 3.0 / 4.0);
		BAB.put("dragon", 1.0);
		BAB.put("elemental", 3.0 / 4.0);
		BAB.put("fey", 1.0 / 2.0);
		BAB.put("giant", 3.0 / 4.0);
		BAB.put("humanoid", 3.0 / 4.0);
		BAB.put("magical beast", 1.0);
		BAB.put("shapechanger", 1.0);
		BAB.put("monstrous humanoid", 1.0);
		BAB.put("ooze", 3.0 / 4.0);
		BAB.put("outsider", 1.0);
		BAB.put("plant", 3.0 / 4.0);
		BAB.put("undead", 1.0 / 2.0);
		BAB.put("vermin", 3.0 / 4.0);
	}

	WeaponFocus(String name) {
		super(name);
	}

	/** Constructor. */
	private WeaponFocus() {
		super("Weapon focus");
	}

	@Override
	public String inform(final Combatant m) {
		return "Base attack bonus: " + m.source.getbaseattackbonus();
	}

	int countattacks(final Monster m) {
		final HashSet<String> attacks = new HashSet<String>();
		for (final List<Attack> as : getattacks(m)) {
			for (final Attack a : as) {
				attacks.add(a.name);
			}
		}
		return attacks.size();
	}

	@Override
	public boolean upgrade(final Combatant c) {
		Monster m = c.source;
		if (m.hasfeat(this) || getattacks(m).isEmpty()
				|| m.getbaseattackbonus() < 1) {
			return false;
		}
		for (int i = 0; i < countattacks(m); i++) {
			super.upgrade(c);
		}
		for (final List<Attack> as : getattacks(m)) {
			for (final Attack a : as) {
				a.bonus += 1;
			}
		}
		return true;
	}

	/**
	 * @return {@link Monster#ranged} or {@link Monster#melee}. The default
	 *         implementation returns <code>null</code> so {@link Feats} can
	 *         read monster with generic Weapon Focus feats.
	 */
	protected List<AttackSequence> getattacks(final Monster m) {
		return null;
	}
}
