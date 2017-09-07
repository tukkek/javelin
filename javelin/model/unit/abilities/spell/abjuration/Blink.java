package javelin.model.unit.abilities.spell.abjuration;

import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.illusion.Displacement;
import javelin.model.unit.attack.Combatant;

/**
 * This isn't really blink but a 1-level lower version of Displacement that can
 * only affect self.
 */
public class Blink extends Displacement {

	public Blink() {
		super("Blink", 2, ChallengeRatingCalculator.ratespelllikeability(2),
				Realm.MAGIC);
		ispotion = true;
		turns = 4;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
