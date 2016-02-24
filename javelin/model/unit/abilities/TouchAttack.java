package javelin.model.unit.abilities;

import java.io.Serializable;

import javelin.controller.upgrade.Upgrade;
import javelin.model.Cloneable;
import javelin.model.unit.Combatant;

public class TouchAttack extends Upgrade implements Cloneable, Serializable {
	public final int[] damage;
	public final int savedc;

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
	public String info(Combatant c) {
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
