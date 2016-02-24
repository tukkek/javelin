package javelin.model.condition;

import javelin.model.Cloneable;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * A condition is a temporary effect on a {@link Combatant}.
 * 
 * @see Combatant#conditions
 * 
 * @author alex
 */
public abstract class Condition implements Cloneable {
	/**
	 * Buff means an effect is benefitial while debuff means it's a penalty.
	 * 
	 * @author alex
	 */
	public enum Effect {
		POSITIVE, NEUTRAL, NEGATIVE
	}

	final float expireat;
	final public Effect effect;
	final public String description;

	public Condition(float expireatp, final Combatant c, final Effect effectp,
			String description) {
		expireat = expireatp;
		effect = effectp;
		this.description = description;
		start(c);
	}

	abstract void start(Combatant c);

	public boolean expire(final Combatant c) {
		if (c.ap > expireat) {
			end(c);
			return true;
		}
		return false;
	}

	abstract void end(Combatant c);

	@Override
	public boolean equals(final Object obj) {
		return getClass().equals(obj.getClass());
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
}
