package javelin.model.spell.necromancy;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Shaken;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Doom extends Spell {

	public Doom() {
		super("Doom", 1, SpellsFactor.ratespelllikeability(1), Realm.EVIL);
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (saved) {
			return target + " resists!";
		}
		target.addcondition(new Shaken(Float.MAX_VALUE, target, casterlevel));
		return target + " is shaken!";
	}

	@Override
	public int save(Combatant caster, Combatant target) {
		return rollsave(target.source.will(), caster);
	}

}
