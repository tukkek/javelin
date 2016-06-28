package javelin.model.unit.abilities;

import java.io.Serializable;

import com.sun.javafx.geom.Area;

import javelin.controller.action.Breath;
import javelin.model.Cloneable;
import javelin.model.unit.Combatant;

/**
 * @see Breath
 * @author alex
 */
public class BreathWeapon implements Serializable, Cloneable {
	/**
	 * @see Area
	 */
	public enum BreathArea {
		CONE, LINE
	}

	public enum SavingThrow {
		FORTITUDE, REFLEXES, WILLPOWER, NO
	}

	final public BreathArea type;
	final public int range;
	final public int[] damage;
	final public String description;
	final public SavingThrow savethrow;
	public int savedc;
	final public float saveeffect;
	final public boolean delay;

	public BreathWeapon(final String description, final BreathArea type,
			final int range, final int die, final int sides, final int bonus,
			final SavingThrow savethrowp, final int savedcp,
			final float saveeffectp, final boolean delayp) {
		super();
		this.type = type;
		this.range = range;
		damage = new int[] { die, sides, bonus };
		this.description = description;
		savethrow = savethrowp;
		savedc = savedcp;
		saveeffect = saveeffectp;
		delay = delayp;
	}

	@Override
	public String toString() {
		return description + " breath (" + range + " feet "
				+ type.name().toLowerCase() + ", " + damage[0] + "d" + damage[1]
				+ (damage[2] >= 0 ? "+" : "") + damage[2] + ")";
	}

	public Integer save(final Combatant target) {
		if (savethrow == SavingThrow.FORTITUDE) {
			final int fortitude = target.source.fortitude();
			return fortitude == Integer.MAX_VALUE ? null : fortitude;
		}
		if (savethrow == SavingThrow.REFLEXES) {
			return target.source.ref;
		}
		if (savethrow == SavingThrow.WILLPOWER) {
			final int will = target.source.will();
			return will == Integer.MAX_VALUE ? null : will;
		}
		if (savethrow == null) {
			return null;
		}
		throw new RuntimeException("Unknown saving throw: " + savethrow);
	}

	@Override
	public BreathWeapon clone() {
		try {
			return (BreathWeapon) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
