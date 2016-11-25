package javelin.model.spell.necromancy;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class SlayLiving extends Touch {

	public SlayLiving() {
		super("Slay living", 5, ChallengeRatingCalculator.ratespelllikeability(5),
				Realm.EVIL);
		castinbattle = true;
		provokeaoo = false;
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		if (saved) {
			target.damage(Math.round(3 * 3.5f + 9), s,
					target.source.energyresistance);
			return target + " resists, is now " + target.getstatus() + ".";
		}
		target.damage(target.hp + 10, s, 0);
		return target + " is killed!";
	}

	@Override
	public int save(final Combatant caster,
			final Combatant target) {
		return rollsave(target.source.fortitude(), caster);
	}

}
