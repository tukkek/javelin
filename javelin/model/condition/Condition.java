package javelin.model.condition;

import javelin.model.unit.Combatant;

public abstract class Condition {
	final float expireat;

	public Condition(float expireatp, final Combatant c) {
		expireat = expireatp;
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

	public abstract String describe();

	@Override
	public boolean equals(final Object obj) {
		return getClass().equals(obj.getClass());
	}
}
