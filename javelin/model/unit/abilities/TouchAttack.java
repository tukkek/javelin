package javelin.model.unit.abilities;

import java.io.Serializable;

import javelin.controller.upgrade.Upgrade;
import javelin.model.Cloneable;
import javelin.model.unit.attack.Combatant;

public class TouchAttack extends Upgrade implements Cloneable, Serializable {
	/** See {@link #TouchAttack(String, int, int, Integer, Integer)}. */
	public final int[] damage;
	/** See {@link #TouchAttack(String, int, int, Integer, Integer)}. */
	public final int savedc;

	/**
	 * Either savedcp or attackbonus must be <code>null</code>.
	 * 
	 * @param die
	 *            Number of die to roll for damage.
	 * @param sides
	 *            Type of die to roll for damage.
	 * @param savedcp
	 *            Will roll against this save DC.
	 */
	public TouchAttack(String namep, int die, int sides, int savedcp) {
		super(namep);
		damage = new int[] { die, sides };
		savedc = savedcp;
	}

	@Override
	public TouchAttack clone() {
		try {
			return (TouchAttack) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String inform(Combatant c) {
		return name + " (" + damage[0] + "d" + damage[1] + ", reflex save "
				+ savedc + " half)";
	}

	@Override
	public boolean apply(Combatant c) {
		c.source.touch = this;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
