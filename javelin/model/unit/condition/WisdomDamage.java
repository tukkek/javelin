package javelin.model.unit.condition;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * TODO convert to a generic AbilityDamage, after abilities and skills have been
 * turned into mapping structures
 * 
 * TODO {@link Poisoned} can be handled like this?
 * 
 * @author alex
 */
public class WisdomDamage extends Condition {
	int damage = 0;
	int timepool;

	public WisdomDamage(Combatant c) {
		super(Float.MAX_VALUE, c, Effect.NEGATIVE, "dummy", null,
				Integer.MAX_VALUE);
	}

	@Override
	public void start(Combatant c) {
		modify(c, -2);
	}

	void modify(Combatant c, int damage) {
		Monster m = c.source.clone();
		c.source = m;
		c.source.raisewisdom(damage);
		this.damage += damage;
	}

	@Override
	public void end(Combatant c) {
	}

	@Override
	public boolean validate(Combatant c) {
		return c.source.wisdom > 2;
	}

	@Override
	public boolean merge(Combatant c, Condition previous) {
		previous.start(c);
		return true;
	}

	@Override
	protected boolean expire(int time, Combatant c) {
		timepool += time;
		while (timepool >= 48 && damage < 0) {
			timepool -= 48;
			modify(c, +2);
		}
		return damage == 0;
	}

	@Override
	public void transfer(Combatant from, Combatant to) {
		to.source.wisdom = from.source.wisdom;
	}
}
