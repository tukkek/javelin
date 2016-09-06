package javelin.model.spell.necromancy;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.condition.Vampiric;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class VampiricTouch extends Touch {

	/** Constructor. */
	public VampiricTouch() {
		super("Vampiric touch", 3, SpellsFactor.ratespelllikeability(3),
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
