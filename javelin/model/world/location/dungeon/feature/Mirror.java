package javelin.model.world.location.dungeon.feature;

import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.condition.Condition;

/**
 * A magic mirror that grants a one-time use of summoning a copy of yourself
 * (expires in 24 hours).
 *
 * @author alex
 */
public class Mirror extends Feature{
	class Reflection extends Spell{
		Reflection(){
			super("Summon reflection",5,0,null);
			castinbattle=true;
			castoutofbattle=false;
		}

		@Override
		public String cast(Combatant caster,Combatant target,boolean saved,
				BattleState s,ChanceNode cn){
			Combatant reflection=caster.clone().clonesource();
			reflection.newid();
			reflection.source.customName="Reflection";
			reflection.spells.remove(this);
			Summon.place(target,reflection,s.getteam(caster),s);
			return "A reflection of "+caster+" appears!";
		}

		@Override
		public void filtertargets(Combatant combatant,List<Combatant> targets,
				BattleState s){
			Spell.targetself(combatant,targets);
		}
	}

	class Mirrorbound extends Condition{
		Mirrorbound(Combatant c){
			super(Float.MAX_VALUE,c,Effect.NEUTRAL,"Mirrorbound",null,24);
			stack=true;
		}

		@Override
		public void start(Combatant c){
			var reflection=c.spells.has(Reflection.class);
			if(reflection==null){
				reflection=new Reflection();
				c.spells.add(reflection);
			}else
				reflection.perday+=1;
		}

		@Override
		public void end(Combatant c){
			var reflection=c.spells.has(Reflection.class);
			if(reflection==null) return;
			if(reflection.perday==1)
				c.spells.remove(reflection);
			else
				reflection.perday-=1;
		}

		@Override
		public void transfer(Combatant from,Combatant to){
			super.transfer(from,to);
			var reflection=from.spells.has(Reflection.class);
			if(reflection!=null&&reflection.exhausted()){
				to.spells.remove(reflection);
				to.removecondition(this);
			}
		}
	}

	/** Java Reflection-friendly constructor. */
	public Mirror(){
		super("dungeonmirror");
		enter=true;
		remove=true;
	}

	@Override
	public boolean activate(){
		var prompt="Do you want to look into the magic mirror?\n";
		prompt+="Press ENTER to confirm or any other key to cancel...";
		if(Javelin.prompt(prompt)!='\n') return false;
		var combatant=selectmember();
		if(combatant==null){
			WorldMove.abort=true;
			return false;
		}
		combatant.addcondition(new Mirrorbound(combatant));
		return true;
	}

	static Combatant selectmember(){
		var squad=Squad.active.members;
		if(squad.size()==1) return squad.get(0);
		var choice=Javelin.choose("Who will look into the mirror?",squad,true,
				false);
		return choice>=0?squad.get(choice):null;
	}
}
