package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;

/**
 * http://www.d20pfsrd.com/magic/all-spells/b/bless/
 *
 * @author alex
 */
public class Bless extends Spell{
	class Blessed extends Condition{
		int bonus=+1;

		Blessed(Combatant c){
			super(c,"blessed",Effect.POSITIVE,1,Float.MAX_VALUE);
		}

		@Override
		public void start(Combatant c){
			c.source=c.source.clone();
			Condition.raiseallattacks(c.source,bonus,0);
		}

		@Override
		public void end(Combatant c){
			c.source=c.source.clone();
			Condition.raiseallattacks(c.source,-bonus,0);
		}
	}

	/** Constructor. */
	public Bless(){
		super("Bless",1,ChallengeCalculator.ratespell(1));
		castonallies=true;
		castinbattle=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		s=s.clone();
		for(Combatant c:s.getteam(caster))
			s.clone(c).addcondition(new Blessed(c));
		return "All allies are blessed!";
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}
}
