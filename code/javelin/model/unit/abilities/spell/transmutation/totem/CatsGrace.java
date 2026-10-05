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
public class CatsGrace extends TotemsSpell{
	class Graceful extends Condition{
		Graceful(Spell s){
			super("graceful",s,Float.MAX_VALUE,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source.changedexteritymodifier(+2);
		}

		@Override
		public void end(Combatant c){
			c.source.changedexteritymodifier(-2);
		}
	}

	/** Constructor. */
	public CatsGrace(){
		super("Cat's grace");
		isrune=new Graceful(this);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Graceful(this));
		return target+"'s dexterity is now "
				+Monster.getsignedbonus(target.source.dexterity)+"!";
	}
}
