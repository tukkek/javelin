package javelin.model.unit;

import java.io.Serializable;

import javelin.controller.Roller;

public class Attack implements Serializable, Cloneable {
	public final String name;
	public int bonus;
	public int[] damage;
	public int threat = -1;
	public int multiplier = -1;

	public Attack(final String name, final int bonusp) {
		this.name = name;
		bonus = bonusp;
	}

	@Override
	public String toString() {
		final String sign = bonus >= 0 ? "+" : "";
		final String threatrange = threat == 20 ? "20" : threat + "-20";
		return name + " " + sign + bonus + " (" + formatDamage() + ", "
				+ threatrange + "x" + multiplier + ")";
	}

	public String formatDamage() {
		return damage[0] + "d" + damage[1] + (damage[2] >= 0 ? "+" : "")
				+ damage[2];
	}

	public int rollDamage() {
		int sum = 0;
		for (int i = 0; i < damage[0]; i++) {
			sum += Roller.rollDie(damage[1]) + damage[2];
		}
		return sum;
	}

	public float getAverageDamageNoBonus() {
		return damage[0] * damage[1] / 2f;
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
