package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * See SRD.
 *
 * @author alex
 */
public class Bane extends Bless{
	class Baned extends Blessed{
		Baned(Spell s){
			super(s);
			description="baned";
			bonus=-1;
			effect=Effect.NEGATIVE;
		}
	}

	/** Constructor. */
	public Bane(){
		name="Bane";
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		String result="";
		for(Combatant c:s.getcombatants())
			if(!c.isally(caster,s)&&getsavetarget(c.source.getwill(),caster)>10){
				c.addcondition(new Baned(this));
				result+=c.toString()+", ";
			}
		return result.isEmpty()?"No creatures were affected..."
				:"Baned: "+result.substring(0,result.length()-2)+".";
	}
}
