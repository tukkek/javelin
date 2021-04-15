package javelin.model.unit.abilities.spell.transmutation;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;
import javelin.model.world.location.dungeon.DungeonFloor;

/**
 * http://www.d20srd.org/srd/spells/longstrider.htm
 *
 * Only castable in battle but will live up to 1 hour if inside a
 * {@link DungeonFloor}. Can't cast outside because it will seem useless for
 * now. TODO allow leveling up spells
 *
 * @author alex
 */
public class Longstrider extends Spell{
	class Strider extends Condition{
		Strider(Spell s){
			super("striding",s,Float.MAX_VALUE,1,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source.walk+=10;
		}

		@Override
		public void end(Combatant c){
			c.source.walk-=10;
		}
	}

	/** Constructor. */
	public Longstrider(){
		super("Longstrider",1,ChallengeCalculator.ratespell(1));
		castinbattle=true;
		castonallies=false;
		ispotion=true;
		isrune=new Strider(this);
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		caster.addcondition(new Strider(this));
		return "Walking speed for "+caster+" is now "+caster.source.walk+"ft!";
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}
}
