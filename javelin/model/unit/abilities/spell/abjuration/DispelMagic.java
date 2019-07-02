package javelin.model.unit.abilities.spell.abjuration;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.condition.Condition;

/**
 * http://www.d20srd.org/srd/spells/dispelMagicGreater.htm
 *
 * @author alex
 */
public class DispelMagic extends Spell{
	/** Constructor. */
	public DispelMagic(){
		super("Greater dispel magic",6,ChallengeCalculator.ratespell(6));
		castoutofbattle=true;
		castinbattle=true;
		isritual=true;
		ispotion=true;
		iswand=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		Summon summon;
		try{
			summon=new Summon(target.source.name,1);
		}catch(RuntimeException e){
			summon=null;
			/* TODO figure out why Summon#ratechallenge is throwin NPE */
		}
		if(target.summoned&&summon!=null&&casterlevel>summon.casterlevel){
			s.remove(target);
			return target+" goes back to its plane of existence!";
		}
		ArrayList<Condition> dispelled=new ArrayList<>();
		for(Condition c:target.getconditions())
			if(c.casterlevel!=null&&casterlevel>c.casterlevel){
				c.dispel();
				target.removecondition(c);
				dispelled.add(c);
			}
		return printconditions(dispelled);
	}

	/**
	 * @return A formatted message informing dispelled conditions, or a proper
	 *         message if given list is empty.
	 */
	static public String printconditions(ArrayList<Condition> dispelled){
		if(dispelled.isEmpty()) return "No conditions were dispelled...";
		String result="";
		for(Condition c:dispelled)
			result+=c.toString()+", ";
		return "The following conditions are dispelled: "
				+result.substring(0,result.length()-2)+"!";
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		return cast(caster,target,true,null,null);
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		// cast on all targets
	}
}
