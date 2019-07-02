package javelin.model.unit.abilities.spell.transmutation.totem;

import javelin.controller.ai.ChanceNode;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class BearsEndurance extends TotemsSpell{
	class Enduring extends Condition{
		Enduring(Combatant c,Integer casterlevelp){
			super(c,"enduring",Effect.POSITIVE,casterlevelp,Float.MAX_VALUE);
		}

		@Override
		public void start(Combatant c){
			c.source.changeconstitutionmodifier(c,2);
		}

		@Override
		public void end(Combatant c){
			c.source.changeconstitutionmodifier(c,-2);
		}
	}

	/** Constructor. */
	public BearsEndurance(){
		super("Bear's endurance",Realm.EARTH);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		target.addcondition(new Enduring(target,casterlevel));
		return target+"'s constitution is now "
				+Monster.getsignedbonus(target.source.constitution)+"!";
	}

}
