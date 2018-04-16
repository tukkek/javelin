package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Shaken;

/**
 * See the d20 SRD for more info.
 */
public class Doom extends Spell {

	public Doom() {
		super("Doom", 1, ChallengeCalculator.ratespelllikeability(1), Realm.EVIL);
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
		return calculatesavedc(target.source.will(), caster);
	}

}
