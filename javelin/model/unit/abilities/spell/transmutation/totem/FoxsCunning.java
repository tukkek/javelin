package javelin.model.unit.abilities.spell.transmutation.totem;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * See the d20 SRD for more info.
 */
public class FoxsCunning extends TotemsSpell{
	class Cunning extends Condition{
		Cunning(Spell s){
			super("cunning",s,Float.MAX_VALUE,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source.intelligence+=4;
		}

		@Override
		public void end(Combatant c){
			c.source.intelligence+=4;
		}
	}

	/** Constructor. */
	public FoxsCunning(){
		super("Fox's cunning");
		isrune=new Cunning(this);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Cunning(this));
		cn.overlay=new AiOverlay(target);
		return target+"'s intelligence is now "
				+Monster.getsignedbonus(target.source.intelligence)+"!";
	}

}
