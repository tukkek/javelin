package javelin.model.unit.abilities.spell.transmutation.totem;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class OwlsWisdom extends TotemsSpell{
	class Wise extends Condition{
		Wise(Spell s){
			super("wise",s,Float.MAX_VALUE,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source.changewisdomscore(+4);
		}

		@Override
		public void end(Combatant c){
			c.source.changewisdomscore(-4);
		}
	}

	/** Constructor. */
	public OwlsWisdom(){
		super("Owl's wisdom");
		isrune=new Wise(this);
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.addcondition(new Wise(this));
		return target+"'s wisdom is now "
				+Monster.getsignedbonus(target.source.wisdom)+"!";
	}

}
