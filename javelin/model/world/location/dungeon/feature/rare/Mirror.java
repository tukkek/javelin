package javelin.model.world.location.dungeon.feature.rare;

import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.world.WorldMove;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.condition.TemporarySpell;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;

/**
 * A magic mirror that grants a one-time use of summoning a copy of yourself
 * (expires in 24 hours).
 *
 * @author alex
 */
public class Mirror extends Feature{
	class Reflection extends Spell{
		Reflection(){
			super("Summon reflection",5,0);
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

	class Mirrorbound extends TemporarySpell{
		Mirrorbound(Combatant c){
			super("Mirrorbound",new Reflection(),c);
		}
	}

	/** Java Reflection-friendly constructor. */
	public Mirror(DungeonFloor f){
		super("mirror");
		enter=true;
		remove=true;
	}

	@Override
	public boolean activate(){
		var prompt="Do you want to look into the magic mirror?\n";
		prompt+="Press ENTER to confirm or any other key to cancel...";
		if(Javelin.prompt(prompt)!='\n') return false;
		var combatant=selectmember("Who will look into the mirror?");
		if(combatant==null){
			WorldMove.abort=true;
			return false;
		}
		combatant.addcondition(new Mirrorbound(combatant));
		return true;
	}

	public static Combatant selectmember(String prompt){
		var squad=Squad.active.members;
		if(squad.size()==1) return squad.get(0);
		var choice=Javelin.choose(prompt,squad,true,false);
		return choice>=0?squad.get(choice):null;
	}
}
