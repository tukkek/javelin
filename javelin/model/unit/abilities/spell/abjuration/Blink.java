package javelin.model.unit.abilities.spell.abjuration;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.illusion.Displacement;
import javelin.model.unit.attack.Combatant;

/**
 * This isn't really blink but a 1-level lower version of Displacement that can
 * only affect self and doesn't cause AoO as a way to simulate the blink Special
 * Quality.
 */
public class Blink extends Displacement {

	public Blink() {
		super("Blink", 2, ChallengeCalculator.ratespelllikeability(2), Realm.MAGIC);
		ispotion = true;
		turns = 4;
		provokeaoo = false;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
