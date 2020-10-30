package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Exhausted;
import javelin.model.unit.condition.Fatigued;

/**
 * Makes a creature exhausted or fatigued on a successful save.
 *
 * @author alex
 */
public class RayOfExhaustion extends Ray{

	/** Constructor. */
	public RayOfExhaustion(){
		super("Ray of exhaustion",3,ChallengeCalculator.ratespell(3));
		castinbattle=true;
		iswand=true;
		isrod=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		Condition c=!saved||target.hascondition(Fatigued.class)!=null
				?new Exhausted(this)
				:new Fatigued(this,0);
		target.addcondition(c);
		return target+" is "+c.description+".";
	}

	@Override
	public int save(Combatant caster,Combatant target){
		return getsavetarget(target.source.getfortitude(),caster);
	}
}
