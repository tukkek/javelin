package javelin.model.condition;

import javelin.model.spell.VampiricRay;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * @see VampiricRay
 * @author alex
 */
public class Vampiric extends Condition {
	final private int steal;
	private Combatant caster;

	public Vampiric(float expireat, Combatant caster, int steal) {
		super(expireat, caster, Effect.NEUTRAL, "vampiric");
		this.caster = caster;
		this.steal = steal;
	}

	@Override
			void start(Combatant c) {
		// see VampiricRay

	}

	@Override
			void end(Combatant c) {
		// doesn't expire
	}

	@Override
	public void finish(BattleState s) {
		caster = s.clone(caster);
		caster.damage(steal, s, 0);
		Game.message(caster + " loses temporary hit points, is now "
				+ caster.getStatus() + ".", null, Delay.BLOCK);
	}
}
