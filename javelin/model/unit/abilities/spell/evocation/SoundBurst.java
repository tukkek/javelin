package javelin.model.unit.abilities.spell.evocation;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Stunned;

/**
 * Blasts an area with a tremendous cacophony.
 *
 * @author alex
 */
public class SoundBurst extends Spell{
	/** Constructor. */
	public SoundBurst(){
		super("Sound burst",2,ChallengeCalculator.ratespelllikeability(2),
				Realm.MAGIC);
		castinbattle=true;
		isscroll=true;
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		for(Combatant c:getradius(target,2,this,s)){
			if(c.equals(caster)) continue;
			target=s.clone(c);
			target.damage(8/2,s,0);
			if(getsavetarget(target.source.getfortitude(),caster)>10)
				target.addcondition(new Stunned(target,casterlevel));
		}
		return caster+" bursts out a tremendous wall of sound!";
	}
}
