package javelin.model.unit.abilities.spell.conjuration.healing;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Poisoned;

/**
 * http://www.d20srd.org/srd/spells/neutralizePoison.htm
 *
 * @author alex
 */
public class NeutralizePoison extends Touch{
	public class Neutralized extends Condition{
		/**
		 * Constructor.
		 *
		 * @param casterlevelp
		 */
		public Neutralized(Combatant c,Integer casterlevelp){
			super(Float.MAX_VALUE,c,Effect.POSITIVE,"immune to poison",casterlevelp,
					1);
		}

		@Override
		public void start(Combatant c){
			Poisoned p=(Poisoned)c.hascondition(Poisoned.class);
			if(p!=null){
				p.neutralized=true;
				c.removecondition(p);
			}
		}

		@Override
		public void end(Combatant c){
			// does nothing
		}
	}

	/** Constructor. */
	public NeutralizePoison(){
		super("Neutralize poison",4,ChallengeCalculator.ratespelllikeability(4),
				Realm.WATER);
		ispotion=true;
		isritual=true;
		castinbattle=true;
		castoutofbattle=true;
		castonallies=true;
		provokeaoo=false;
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		boolean engaged=s.isengaged(combatant);
		for(Combatant c:new ArrayList<>(targets)){
			if(!Touch.isfar(combatant,c)){
				final boolean ally=combatant.isally(c,s);
				final boolean poisonerenemy=!ally&&(checkpoisoner(c,c.source.melee)
						||checkpoisoner(c,c.source.ranged));
				if(ally&&!engaged||poisonerenemy) continue;
			}
			targets.remove(c);
		}
	}

	boolean checkpoisoner(Combatant c,ArrayList<AttackSequence> attacks){
		for(AttackSequence sequence:attacks)
			for(Attack a:sequence)
				if(a.geteffect() instanceof Poison) return true;
		return false;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		if(target.isally(caster,s)) return castpeacefully(caster,target,null);
		neutralize(target.source.melee);
		neutralize(target.source.ranged);
		return target+" is not poisonous anymore!";
	}

	void neutralize(ArrayList<AttackSequence> attacks){
		for(AttackSequence sequence:attacks)
			for(Attack a:sequence){
				Spell effect=a.geteffect();
				if(Poison.class.isInstance(effect)) effect=null;
			}
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		target.addcondition(new Neutralized(target,casterlevel));
		return target+" is immune to poison!";
	}
}
