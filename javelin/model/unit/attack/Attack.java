package javelin.model.unit.attack;

import java.io.Serializable;

import javelin.Javelin;
import javelin.controller.db.reader.fields.Damage;
import javelin.model.Cloneable;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;

/**
 * A single attack in an {@link AttackSequence}.
 * 
 * @author alex
 */
public class Attack implements Serializable, Cloneable {
	/** Attack description. */
	public final String name;
	/**
	 * Attack bonus as it appears on the XML data/stat block.
	 */
	public int bonus;
	/**
	 * Format by index: 0d1+2
	 */
	public int[] damage = null;
	/** Critical hit range (usually only on a natural 20). */
	public int threat = -1;
	/**
	 * Number of times damage should be multiplied for in case of critical hit.
	 */
	public int multiplier = -1;
	/**
	 * If changed to <code>true</code> will use energy resistance instead of
	 * damage reducton to absorb attack.
	 * 
	 * @see Monster#energyresistance
	 */
	public boolean energy = false;
	/**
	 * This spell will be cast upon hitting with attack.
	 * 
	 * @see Damage
	 */
	public Spell effect = null;
	/**
	 * If <code>true</code> ignores armor class.
	 */
	public boolean touch = false;

	/** Constructor. */
	public Attack(final String name, final int bonusp, boolean touch) {
		this.name = name;
		bonus = bonusp;
		this.touch = touch;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	/**
	 * @param target
	 *            Also shows how likely this attack is to hit.
	 */
	public String toString(Combatant target) {
		String chance;
		if (target == null) {
			chance = (bonus >= 0 ? "+" : "") + bonus;
		} else {
			chance = Javelin.translatetochance(target.ac() - bonus) + " to hit";
		}
		return name + " (" + chance + ", " + formatDamage() + ", "
				+ (threat == 20 ? "20" : threat + "-20") + "/x" + multiplier
				+ ")";
	}

	public String formatDamage() {
		if (damage == null) {
			return "null";
		}
		String output = damage[0] + "d" + damage[1]
				+ (damage[2] >= 0 ? "+" : "") + damage[2];
		if (energy) {
			output += " energy";
		}
		if (effect != null) {
			output += ", " + effect.name;
		}
		return output;
	}

	/**
	 * @return Average damage with elemental but no bonus.
	 * @see #damage
	 */
	public float getAverageDamageNoBonus() {
		return damage[0] * damage[1] / 2f;
	}

	/**
	 * @return Non-elemental damage.
	 */
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
}
