package javelin.model.unit.condition;

import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.necromancy.VampiricTouch;
import javelin.model.unit.attack.Combatant;

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

	@Override
	public boolean merge(Combatant c, Condition previous) {
		previous.expireat = Math.max(expireat, previous.expireat);
		return true;
	}
}
