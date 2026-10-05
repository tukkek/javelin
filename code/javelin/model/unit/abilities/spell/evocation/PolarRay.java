package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Ray;
import javelin.old.RPG;

/**
 * A blue-white ray of freezing air and ice springs from your hand. The ray
 * deals 15d6 points of energy damage.
 *
 * @author alex
 */
public class PolarRay extends Ray{
	/** Constructor. */
	public PolarRay(){
		super("Polar ray",8,ChallengeCalculator.ratespell(8));
		castinbattle=true;
		iswand=true;
		isrod=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.damage(RPG.average(15,6),target.source.energyresistance,s);
		return target+" is "+target.getstatus()+"!";
	}
}
