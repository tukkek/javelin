package javelin.model.unit.abilities.spell.evocation;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Produces a vertical column of divine fire.
 *
 * @author alex
 */
public class FlameStrike extends Spell{
	/** Constructor. */
	public FlameStrike(){
		super("Flame strike",5,ChallengeCalculator.ratespell(5));
		castinbattle=true;
		iswand=true;
		isrod=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		for(Combatant c:getradius(target,2,this,s))
			s.clone(c).damage(casterlevel*6/2,s,c.source.energyresistance);
		return "A roaring column of fire descends around "+target+"!";
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		// all targets is fine
	}
}
