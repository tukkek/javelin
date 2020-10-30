package javelin.model.unit.abilities.spell.evocation;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Stunned;
import javelin.old.RPG;

/**
 * Blasts an area with a tremendous cacophony.
 *
 * @author alex
 */
public class SoundBurst extends Spell{
	/** Constructor. */
	public SoundBurst(){
		super("Sound burst",2,ChallengeCalculator.ratespell(2));
		castinbattle=true;
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		for(Combatant c:getradius(caster,2,this,s)){
			c=s.clone(c);
			c.damage(RPG.average(1,8),s,0);
			if(getsavetarget(c.source.getfortitude(),caster)>10)
				c.addcondition(new Stunned(c,this));
		}
		return caster+" bursts out a tremendous wall of sound!";
	}
}
