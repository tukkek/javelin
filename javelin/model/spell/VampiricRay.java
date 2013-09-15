//package javelin.model.spell;
//
//import javelin.controller.exception.NotPeaceful;
//import javelin.controller.factor.SpellsFactor;
//import javelin.model.state.BattleState;
//import javelin.model.unit.Combatant;
//import tyrant.mikera.engine.RPG;
//
//public class VampiricRay extends Ray {
//
//	/**
//	 * If we want the HP gain to be permanent then it's like Vampiric Touch
//	 * (level 3 spell converted from touch to ray, average 21 damage) plus cure
//	 * critical wounds (average 25 cure). There is also the fact that both are
//	 * cast simultaneously instead of at separate actions so we apply Quicken
//	 * Spell (+4 levels) to the cure spell. Since we can discard the temporary
//	 * HP element of Vampiric Touch we'll subtract 1 level form the spell.
//	 */
//	public VampiricRay(final String name) {
//		super(name + "vampiric ray", SpellsFactor
//				.calculatechallengefortouchspellconvertedtoray(3 - 1)
//				+ SpellsFactor.calculatechallengefortouchattackability(4 + 4),
//				false, false);
//	}
//
//	@Override
//	public String cast(final Combatant caster, final Combatant target,
//			final BattleState s, final boolean saved) {
//		int steal = 0;
//		for (int i = 0; i < 6; i++) {
//			steal += RPG.r(1, 6);
//		}
//		final int max = target.hp + 10;
//		if (steal > max) {
//			steal = max;
//		}
//		caster.hp += target.damage(steal, s, target.source.resistance);
//		if (caster.hp > caster.maxhp) {
//			caster.hp = caster.maxhp;
//		}
//		return describe(target) + "\n" + describe(caster);
//	}
//
//	public String describe(final Combatant c) {
//		return c + " is " + c.getStatus();
//	}
//
//	@Override
//	public int calculatesavetarget(final Combatant caster,
//			final Combatant target) {
//		return Integer.MAX_VALUE;
//	}
//
//	@Override
//	public String castpeacefully(final Combatant caster,
//			final Combatant combatant) throws NotPeaceful {
//		throw new NotPeaceful();
//	}
// }
