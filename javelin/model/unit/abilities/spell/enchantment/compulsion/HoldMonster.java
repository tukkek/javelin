package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Paralyzed;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/** See the d20 SRD for more info. */
public class HoldMonster extends Spell{
	/** Constructor. */
	public HoldMonster(){
		super("Hold monster",5,.45f);
		castinbattle=true;
		iswand=true;
		isrod=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		if(saved) return target+" resists.";
		var duration=calculateduration(target.source.getwill(),caster);
		target.addcondition(new Paralyzed(caster.ap+duration,target,casterlevel));
		if(cn!=null) cn.overlay=new AiOverlay(target);
		return target+" is paralyzed for "+duration+" turns!";
	}

	@Override
	public int save(Combatant caster,Combatant target){
		if(target.source.immunitytoparalysis) return Integer.MIN_VALUE;
		return getsavetarget(target.source.getwill(),caster);
	}
}
