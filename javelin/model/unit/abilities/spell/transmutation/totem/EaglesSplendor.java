package javelin.model.unit.abilities.spell.transmutation.totem;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class EaglesSplendor extends TotemsSpell{
	class Splendid extends Condition{
		Splendid(Combatant c,Integer casterlevelp){
			super(c,"splendid",Effect.POSITIVE,casterlevelp,Float.MAX_VALUE);
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
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Splendid(target,casterlevel));
		return target+"'s charisma is now "
				+Monster.getsignedbonus(target.source.charisma)+"!";
	}
}
