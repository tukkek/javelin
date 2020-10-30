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
public class EaglesSplendor extends TotemsSpell{
	class Splendid extends Condition{
		Splendid(Spell s){
			super("splendid",s,Float.MAX_VALUE,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source.charisma+=4;
		}

		@Override
		public void end(Combatant c){
			c.source.charisma-=4;
		}
	}

	/** Constructor. */
	public EaglesSplendor(){
		super("Eagle's splendor");
		isrune=new Splendid(this);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Splendid(this));
		return target+"'s charisma is now "
				+Monster.getsignedbonus(target.source.charisma)+"!";
	}
}
