package javelin.model.condition;

import javelin.model.spell.necromancy.VampiricTouch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * @see VampiricTouch
 * @author alex
 */
public class Vampiric extends Condition {
	final private int steal;

	public Vampiric(float expireat, Combatant caster, int steal,
			Integer casterlevelp) {
		super(expireat, caster, Effect.POSITIVE, "vampiric", casterlevelp, 1);
		this.steal = steal;
		stacks = true;
	}

	@Override
	public void start(Combatant c) {
		// see VampiricTouch

	}

	@Override
	public void end(Combatant c) {
		c.hp -= steal;
		if (c.hp < 1) {
			c.hp = 1;
		}
	}

	@Override
	public void finish(BattleState s) {
		// Game.message(caster + " loses temporary hit points, is now "
		// + caster.getStatus() + ".", null, Delay.BLOCK);
	}
}
