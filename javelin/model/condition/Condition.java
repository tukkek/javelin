package javelin.model.condition;

import java.io.Serializable;

import javelin.model.Cloneable;
import javelin.model.spell.abjuration.DispelMagic;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * A condition is a temporary effect on a {@link Combatant}.
 *
 * @see Combatant#conditions
 *
 * @author alex
 */
public abstract class Condition implements Cloneable, Serializable {
	/**
	 * Buff means an effect is benefitial while debuff means it's a penalty.
	 *
	 * @author alex
	 */
	public enum Effect {
		POSITIVE, NEUTRAL, NEGATIVE
	}

	/**
	 * AP at which this effect will wear off. Use {@link Float#MAX_VALUE} to
	 * make it permanent for the duration of battle.
	 */
	public float expireat;
	/** @see Effect */
	final public Effect effect;
	/** Short description. */
	final public String description;
	/**
	 * The number of hours for this to persist after battle. If
	 * <code>null</code> will call {@link #end(Combatant)} at the end of combat.
	 * If 0 will stop at the end of combat but affect the original
	 * {@link Combatant} the one used in-battle was cloned from.
	 */
	public Integer longterm;
	/**
	 * If <code>false</code> will extend {@link #expireat} of pre-existing
	 * condition instead of adding and applying a new instance.
	 */
	public boolean stacks = false;
	/**
	 * <code>null</code> if this is not magical and can't be affected by
	 * {@link DispelMagic}.
	 */
	public Integer casterlevel;

	public Condition(float expireatp, final Combatant c, final Effect effectp,
			String description, Integer casterlevel) {
		this(expireatp, c, effectp, description, casterlevel, null);
	}

	/** See fields. */
	public Condition(float expireatp, Combatant c, Effect effectp,
			String descriptionp, Integer casterlevel, Integer longtermp) {
		expireat = expireatp;
		effect = effectp;
		description = descriptionp;
		longterm = longtermp;
		this.casterlevel = casterlevel;
		if (!stacks) {
			Condition preexisting = c.hascondition(getClass());
			if (preexisting != null) {
				preexisting.expireat = Math.max(expireatp,
						preexisting.expireat);
				return;
			}
		}
	}

	public abstract void start(Combatant c);

	public boolean expire(final Combatant c) {
		if (c.ap > expireat) {
			c.removecondition(this);
			return true;
		}
		return false;
	}

	public abstract void end(Combatant c);

	@Override
	public boolean equals(final Object obj) {
		return getClass().equals(obj.getClass());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public void finish(BattleState s) {
		// does nothing by default
	}

	@Override
	public Cloneable clone() {
		try {
			return (Cloneable) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Migrates a temporary condition to the original {@link Combatant} instance
	 * outside of battle.
	 *
	 * @param to
	 *            The original combatant.
	 * @param to2
	 */
	public void transfer(Combatant from, Combatant to) {
		// nothing by default
	}

	/**
	 * @param time
	 *            Elapsed time in hours.
	 */
	public void terminate(int time, Combatant c) {
		if (longterm == null) {
			return;
		}
		longterm -= time;
		if (longterm <= 0) {
			c.removecondition(this);
		}
	}

	@Override
	public String toString() {
		return description;
	}

	/**
	 * This is called when a condition is removed by {@link DispelMagic}, mostly
	 * as a way to prevent negative effects from happening in
	 * {@link #end(Combatant)}, when applicable.
	 *
	 * See {@link Poisoned} as an example.
	 */
	public void dispel() {
		// nothing
	}
}
