package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.condition.Condition;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Based on the spell Dominate Monster but trades the duration (1 day/level) to
 * a single battle and to maintain spell-level balance cuts out all the costs of
 * redirecting and commanding the enchanted target.
 *
 * The enchanted {@link Monster} still gets its +2 saving throw every round,
 * represented by {@link #calculateduration(int, Combatant)}.
 *
 * It's not really a ray but we're abusing the existing logic here because it's
 * a lot easier.
 */
public class DominateMonster extends Ray{
	/**
	 * A {@link Monster} controlled by {@link HoldMonster}.
	 *
	 * @author alex
	 */
	public class Dominated extends Condition{
		Combatant target;

		/** Constructor. */
		public Dominated(float expireatp,Combatant c,Integer casterlevelp){
			super(c,"dominated",Effect.NEUTRAL,casterlevelp,expireatp);
			target=c;
		}

		@Override
		public void start(Combatant c){
			/* can't access here so use #switchteams */
		}

		@Override
		public void end(Combatant c){
			// see #finish
		}

		@Override
		public void finish(BattleState s){
			target=s.clone(target);
			switchteams(target,s);
		}
	}

	/** Constructor. */
	public DominateMonster(){
		super("Dominate monster",9,ChallengeCalculator.ratespell(9),Realm.EVIL);
		automatichit=true;
		apcost=1;
		castinbattle=true;
		apcost=1;
		iswand=true;
		isrod=true;
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		cn.overlay=new AiOverlay(target.getlocation());
		if(saved) return target+" resists!";
		switchteams(target,s);
		int duration=calculateduration(target.source.getwill()+2,caster);
		var d=new Dominated(duration,target,casterlevel);
		target.addcondition(d);
		return "Dominated "+target+" for "+duration+" round(s)!";
	}

	@Override
	public int save(final Combatant caster,final Combatant target){
		return getsavetarget(target.source.getwill(),caster);
	}

	@Override
	public float getsavechance(Combatant caster,Combatant target){
		float first=super.getsavechance(caster,target);
		if(first==0||first==1) return first;
		float second=Action.bind(first+.1f);
		/*
		 * chance of either passing the first or not passing the first but
		 * passing the second:
		 */
		return first+second-first*second;
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		super.filtertargets(combatant,targets,s);
		for(Combatant c:new ArrayList<>(targets))
			if(c.source.immunitytomind) targets.remove(c);
	}

	static void switchteams(Combatant target,BattleState s){
		ArrayList<Combatant> from=s.getteam(target);
		from.remove(target);
		(from==s.redTeam?s.blueTeam:s.redTeam).add(target);
	}
}
