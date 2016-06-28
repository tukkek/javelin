package javelin.model.spell.transmutation;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Darkvision extends Touch {
	/** Constructor. */
	public Darkvision() {
		super("Darkvision", 2, SpellsFactor.ratespelllikeability(2),
				Realm.EVIL);
		castinbattle = true;
		castonallies = true;
		castoutofbattle = true;
		ispotion = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.addcondition(
				new javelin.model.condition.Darkvision(target, casterlevel));
		return target + "'s eyes glow!";
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		return castpeacefully(caster, target);
	}
}
