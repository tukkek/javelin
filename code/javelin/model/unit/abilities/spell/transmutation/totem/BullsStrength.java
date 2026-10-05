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
public class BullsStrength extends TotemsSpell{
	class Strong extends Condition{
		Strong(Spell s){
			super("strong",s,Float.MAX_VALUE,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source.changestrengthmodifier(+2);
		}

		@Override
		public void end(Combatant c){
			c.source.changestrengthmodifier(-2);
		}
	}

	/** Constructor. */
	public BullsStrength(){
		super("Bull's strength");
		isrune=new Strong(this);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Strong(this));
		return target+"'s strength is now "
				+Monster.getsignedbonus(target.source.strength)+"!";
	}

}
