package javelin.model.spell.wounds;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.spell.Ray;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class CureModerateWounds extends Ray {
	/**
	 * @XdY+Z
	 */
	final int[] rolldata;

	public CureModerateWounds(final String name, final float incrementcost,
			int[] rolldatap, int casterlevelp) {
		super(name, incrementcost, true, true, casterlevelp);
		rolldata = rolldatap;
	}

	public CureModerateWounds(final String string) {
		this(string + "cure moderate wounds", SpellsFactor
				.calculatechallengefortouchspellconvertedtoray(2), new int[] {
				2, 8, 4 }, 3);
	}

	@Override
	public String cast(final Combatant caster, final Combatant targetCombatant,
			final BattleState s, final boolean saved) {
		final int heal = rolldata[0] * rolldata[1] / 2 + rolldata[2];
		targetCombatant.hp += heal;
		if (targetCombatant.hp >= targetCombatant.maxhp) {
			targetCombatant.hp = targetCombatant.maxhp;
		}
		return targetCombatant + " is now " + targetCombatant.getStatus();
	}

	@Override
	public int calculatesavetarget(final Combatant caster,
			final Combatant target) {
		return -Integer.MAX_VALUE;
	}

	@Override
	public String castpeacefully(final Combatant caster,
			final Combatant combatant) {
		return cast(caster, combatant, null, false);
	}
}
