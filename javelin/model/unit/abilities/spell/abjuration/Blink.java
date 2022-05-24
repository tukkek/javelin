package javelin.model.unit.abilities.spell.abjuration;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.illusion.Displacement;

/**
 * This isn't really blink but a 1-level lower version of Displacement that can
 * only affect self and doesn't cause AoO as a way to simulate the blink Special
 * Quality.
 */
public class Blink extends Displacement{
	/** Constructor. */
	public Blink(){
		super("Blink",2,ChallengeCalculator.ratespell(2));
		turns=4;
		provokeaoo=false;
		ispotion=true;
	}

	@Override
	public void filter(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}
}
