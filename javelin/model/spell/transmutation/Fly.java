package javelin.model.spell.transmutation;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Fly extends Touch {
	/** Constructor. */
	public Fly() {
		super("Fly", 3, SpellsFactor.ratespelllikeability(3), Realm.WIND);
		castinbattle = true;
		castonallies = true;
		ispotion = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.addcondition(
				new javelin.model.condition.Flying(target, casterlevel));
		return target + " floats above the ground!";
	}
}
