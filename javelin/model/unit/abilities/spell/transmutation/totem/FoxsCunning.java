package javelin.model.unit.abilities.spell.transmutation.totem;

import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.condition.Condition;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * See the d20 SRD for more info.
 */
public class FoxsCunning extends TotemsSpell{
	class Cunning extends Condition{
		Cunning(Combatant c,Integer casterlevelp){
			super(c,"cunning",Effect.POSITIVE,casterlevelp,Float.MAX_VALUE);
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
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Cunning(target,casterlevel));
		cn.overlay=new AiOverlay(target);
		return target+"'s intelligence is now "
				+Monster.getsignedbonus(target.source.intelligence)+"!";
	}

}
