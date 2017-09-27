package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class SlayLiving extends Touch {
	public SlayLiving() {
		super("Slay living", 5, CrCalculator.ratespelllikeability(5),
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
	public int save(final Combatant caster, final Combatant target) {
		return calculatesavedc(target.source.fortitude(), caster);
	}

}
