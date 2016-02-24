package javelin.model.spell;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class SlayLiving extends Ray {

	public SlayLiving() {
		super("Slay living", SpellsFactor.ratetouchconvertedtoray(5), false,
				false, 5);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		if (saved) {
			target.damage(Math.round(3 * 3.5f + 9), s,
					target.source.resistance);
			return target + " resists, is now " + target.getStatus() + ".";
		}
		target.damage(target.hp + 10, s, 0);
		return target + " is killed!";
	}

	@Override
	public int calculatesavetarget(final Combatant caster,
			final Combatant target) {
		return save(5, target.source.fort, caster);
	}

	@Override
	public String castpeacefully(final Combatant caster,
			final Combatant combatant) throws NotPeaceful {
		throw new NotPeaceful();
	}

}
