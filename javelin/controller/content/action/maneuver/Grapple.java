package javelin.controller.content.action.maneuver;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.Constrict;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;

/**
 * TODO since one of the combatants can die while grappling due to constriction,
 * it would make sense to do some turn-by-turn calculation of damage instead to
 * prevent extra damage in the case of a combatant diying on the first turn of a
 * 3-turn grapple. the turn-by-turn doesn't need to be handled by game flow,
 * just when applying the damagein
 * {@link #hit(Combatant, Combatant, BattleState, float)}.
 *
 * TODO now that Conditions are more flexible, make a turn-by-turn grapple
 * dynamic. for now just making grapple last a turn because it can becomes
 * pretty silly when a single monster grapples for 9 turns and dies doing
 * nothing.
 *
 * @author alex
 */
public class Grapple extends ExpertiseAction{
	public static final Action INSTANCE=new Grapple();

	public class Grappling extends Condition{
		public Grappling(float expireatp,Combatant c){
			super("grappling",null,expireatp,Effect.NEGATIVE);
		}

		@Override
		public void start(Combatant c){
			c.acmodifier-=2;
		}

		@Override
		public void end(Combatant c){
			c.acmodifier+=2;
		}
	}

	private Grapple(){
		super("Grapple","G",ImprovedGrapple.SINGLETON,+2);
	}

	@Override
	boolean validatetarget(Combatant target){
		return target.hascondition(Grappling.class)==null;
	}

	@Override
	ChanceNode miss(Combatant combatant,Combatant target,BattleState battleState,
			float chance){
		return new ChanceNode(battleState,chance,"Grapple attempt fails...",
				Javelin.Delay.WAIT);
	}

	@Override
	ChanceNode hit(Combatant current,Combatant target,BattleState s,float chance){
		s=s.clone();
		current=s.clone(current);
		target=s.clone(target);
		int duration=Math
				.round(1/calculatesavechance(current,calculatesavebonus(target)));
		if(duration<1) duration=1;
		//		var duration=1;
		String message=current+" grapples "+target+" for "+duration+" turn(s)!";
		message+=constrict(current,target,duration,s);
		message+=constrict(target,current,duration,s);
		grapple(current,duration);
		grapple(target,duration);
		return new ChanceNode(s,chance,message,Javelin.Delay.BLOCK);
	}

	void grapple(Combatant c,int duration){
		if(c.hp>0){
			c.ap+=duration;
			c.addcondition(new Grappling(c.ap+.1f,c));
		}
	}

	String constrict(Combatant current,Combatant target,int duration,
			BattleState s){
		Constrict c=current.source.constrict;
		if(c==null) return "";
		target.damage(c.damage*duration,c.energy?target.source.energyresistance:target.source.dr,
				s);
		return "\n"+target+" is "+target.getstatus()+".";
	}

	@Override
	int getsavebonus(Combatant targetCombatant){
		return targetCombatant.source.fort;
	}

	@Override
	int getattackerbonus(Combatant combatant){
		return Monster.getbonus(combatant.source.strength);
	}
}
