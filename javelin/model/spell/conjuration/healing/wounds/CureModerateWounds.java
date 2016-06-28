package javelin.model.spell.conjuration.healing.wounds;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class CureModerateWounds extends Touch {
	/**
	 * @XdY+Z
	 */
	final int[] rolldata;

	public CureModerateWounds(final String name, final float incrementcost,
			int[] rolldatap, int levelp) {
		super(name, levelp, incrementcost, Realm.WATER);
		rolldata = rolldatap;
		castonallies = true;
		castoutofbattle = true;
		castinbattle = true;
		isritual = true;
		ispotion = true;
	}

	public CureModerateWounds() {
		this("Cure moderate wounds", SpellsFactor.ratespelllikeability(2),
				new int[] { 2, 8, 4 }, 2);
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
	public String castpeacefully(final Combatant caster,
			final Combatant combatant) {
		return cast(caster, combatant, null, false);
	}
}
