package javelin.model.spell;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.condition.Vampiric;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class VampiricRay extends Ray {

	/**
	 * If we want the HP gain to be permanent then it's like Vampiric Touch
	 * (level 3 spell converted from touch to ray, average 21 damage) plus cure
	 * critical wounds (average 25 cure). There is also the fact that both are
	 * cast simultaneously instead of at separate actions so we apply Quicken
	 * Spell (+4 levels) to the cure spell. Since we can discard the temporary
	 * HP element of Vampiric Touch we'll subtract 1 level form the spell.
	 */
	public VampiricRay() {
		super("vampiric ray", SpellsFactor.ratetouchconvertedtoray(3), false,
				false, 3);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		int steal = 21;
		final int max = target.hp + 10;
		if (steal > max) {
			steal = max;
		}
		target.damage(steal, s, target.source.resistance);
		final int originalhp = caster.hp;
		caster.hp += steal;
		if (caster.hp > caster.maxhp) {
			caster.hp = caster.maxhp;
		}
		caster.conditions.add(
				new Vampiric(Float.MAX_VALUE, caster, caster.hp - originalhp));
		return describe(target) + "\n" + describe(caster);
	}

	public String describe(final Combatant c) {
		return c + " is " + c.getStatus();
	}

	@Override
	public int calculatesavetarget(final Combatant caster,
			final Combatant target) {
		return Integer.MAX_VALUE;
	}

	@Override
	public String castpeacefully(final Combatant caster,
			final Combatant combatant) throws NotPeaceful {
		throw new NotPeaceful();
	}
}
