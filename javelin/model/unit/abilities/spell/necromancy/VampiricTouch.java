package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Vampiric;

/**
 * See the d20 SRD for more info.
 */
public class VampiricTouch extends Touch {

	/** Constructor. */
	public VampiricTouch() {
		super("Vampiric touch", 3, ChallengeRatingCalculator.ratespelllikeability(3),
				Realm.EVIL);
		castinbattle = true;
		provokeaoo = false;
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		int steal = 21;
		final int max = target.hp + 10;
		if (steal > max) {
			steal = max;
		}
		target.damage(steal, s, target.source.energyresistance);
		final int originalhp = caster.hp;
		caster.hp += steal;
		if (caster.hp > caster.maxhp) {
			caster.hp = caster.maxhp;
		}
		caster.addcondition(new Vampiric(Float.MAX_VALUE, caster,
				caster.hp - originalhp, casterlevel));
		return describe(target) + "\n" + describe(caster);
	}

	public String describe(final Combatant c) {
		return c + " is " + c.getstatus();
	}

}
