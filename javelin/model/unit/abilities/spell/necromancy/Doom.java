package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Shaken;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * See the d20 SRD for more info.
 */
public class Doom extends Spell{
	public Doom(){
		super("Doom",1,ChallengeCalculator.ratespelllikeability(1));
		castinbattle=true;
		isscroll=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		if(cn!=null) cn.overlay=new AiOverlay(target.getlocation());
		if(saved) return target+" resists!";
		target.addcondition(new Shaken(Float.MAX_VALUE,target,casterlevel));
		return target+" is shaken!";
	}

	@Override
	public int save(Combatant caster,Combatant target){
		return getsavetarget(target.source.getwill(),caster);
	}
}
