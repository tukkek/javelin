package javelin.model.condition;

import javelin.controller.upgrade.skill.Heal;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * A poisoned unit takes a certain amount of constitution damage immediately and
 * a secondary amount after a short while.
 * 
 * @see Monster#raiseconstitution(Combatant, int)
 * @author alex
 */
public class Poisoned extends Condition {

	int secondary;
	int dc;
	/** See {@link Neutralized}. */
	public boolean neutralized;

	/**
	 * Constructor.
	 * 
	 * @param secondary
	 *            The positive amount of secondary damage to cause.
	 * @param dcp
	 *            DC for a {@link Heal} check to ignore secondary effects of the
	 *            poison.
	 * @param casterlevelp
	 * 
	 * @see Monster#raiseconstitution(Combatant, int)
	 */
	public Poisoned(float expireatp, Combatant c, Effect effectp, int secondary,
			int dcp, Integer casterlevelp) {
		super(expireatp, c, effectp, "poisoned", casterlevelp, 1);
		this.secondary = secondary;
		this.dc = dcp;
		neutralized = c.hascondition(Neutralized.class) != null;
	}

	@Override
	public void start(Combatant c) {
		if (neutralized) {
			c.removecondition(this);
		} else {
			damage(c, 3);
		}
	}

	void damage(Combatant c, int d) {
		int original = c.source.constitution;
		if (!c.source.raiseconstitution(c, -d)) {
			c.hp = 0;
		}
		c.source.poison += (original - c.source.constitution) / 2;
	}

	@Override
	public void end(Combatant c) {
		if (!neutralized && c.heal() >= dc) {
			damage(c, secondary);
		}
	}

	@Override
	public void transfer(Combatant from, Combatant to) {
		to.maxhp = from.maxhp;
		to.source.poison = from.source.poison;
		to.source.raiseconstitution(to, -to.source.poison);
		to.hp = from.hp;
	}

	@Override
	public void dispel() {
		neutralized = true;
	}
}
