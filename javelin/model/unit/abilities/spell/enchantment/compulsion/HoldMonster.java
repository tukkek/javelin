package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Paralyzed;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * See the d20 SRD for more info.
 */
public class HoldMonster extends Spell{

	public HoldMonster(){
		super("Hold monster",5,.45f,Realm.MAGIC);
		castinbattle=true;
		isscroll=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		if(saved) return target+" resists.";
		int turns=getsavetarget(0,caster)-10-target.source.getwill();
		if(turns>9)
			turns=9;
		else if(turns<1) turns=1;
		target.addcondition(new Paralyzed(caster.ap+turns,target,casterlevel));
		cn.overlay=new AiOverlay(target);
		return target+" is paralyzed for "+turns+" turns!";
	}

	@Override
	public int save(Combatant caster,Combatant target){
		if(target.source.immunitytoparalysis) return Integer.MIN_VALUE;
		return getsavetarget(target.source.getwill(),caster);
	}
}
