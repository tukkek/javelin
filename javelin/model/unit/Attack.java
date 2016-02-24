package javelin.model.unit;

import java.io.Serializable;

import javelin.Javelin;
import javelin.model.Cloneable;

/**
 * A single attack in an {@link AttackSequence}.
 * 
 * @author alex
 */
public class Attack implements Serializable, Cloneable {
	public final String name;
	public int bonus;
	/**
	 * Format by index: 0d1+2
	 */
	public int[] damage;
	public int threat = -1;
	public int multiplier = -1;

	public Attack(final String name, final int bonusp) {
		this.name = name;
		bonus = bonusp;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	public String toString(Combatant target) {
		String chance;
		if (target == null) {
			chance = (bonus >= 0 ? "+" : "") + bonus;
		} else {
			chance = Javelin.translatetochance(target.ac() - bonus) + " to hit";
		}
		return name + " (" + chance + ", " + formatDamage() + ", "
				+ (threat == 20 ? "20" : threat + "-20") + "x" + multiplier
				+ ")";
	}

	public String formatDamage() {
		return damage[0] + "d" + damage[1] + (damage[2] >= 0 ? "+" : "")
				+ damage[2];
	}

	// public int rollDamage() {
	// int sum = 0;
	// for (int i = 0; i < damage[0]; i++) {
	// sum += Roller.rollDie(damage[1]) + damage[2];
	// }
	// return sum;
	// }

	public float getAverageDamageNoBonus() {
		return damage[0] * damage[1] / 2f;
	}

	public int getaveragedamage() {
		return damage[0] * damage[1] / 2 + damage[2];
	}

	@Override
	public Attack clone() {
		try {
			final Attack clone = (Attack) super.clone();
			clone.damage = damage.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	// @Override
	// public boolean equals(final Object obj) {
	// // if (!(obj instanceof Attack)) {
	// // return false;
	// // }
	// final Attack a = (Attack) obj;
	// return a.bonus == bonus && a.damage[0] == damage[0]
	// && a.damage[1] == damage[1] && a.damage[2] == damage[2]
	// && a.name.equals(name) && a.threat == threat
	// && a.multiplier == multiplier;
	// }
}
