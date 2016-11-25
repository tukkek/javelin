package javelin.model.spell.abjuration;

import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Blinking;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Blink extends Spell {

	public Blink() {
		super("Blink", 3, ChallengeRatingCalculator.ratespelllikeability(3), Realm.MAGIC);
		castinbattle = true;
		super.ispotion = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		caster.addcondition(new Blinking(caster.ap + 5, caster, casterlevel));
		return caster + " is blinking!";
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
