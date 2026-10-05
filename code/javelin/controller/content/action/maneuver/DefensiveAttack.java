package javelin.controller.content.action.maneuver;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.Action;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.unit.skill.Skill;

public class DefensiveAttack extends ExpertiseAction{
	public static final Action INSTANCE=new DefensiveAttack();

	public class DefensiveStance extends Condition{

		private int acbonus;

		public DefensiveStance(float expireatp,Combatant c,int acbonus){
			super("defensive stance",null,expireatp,Effect.POSITIVE);
			this.acbonus=acbonus;
		}

		@Override
		public void start(Combatant c){
			c.acmodifier+=acbonus;
		}

		@Override
		public void end(Combatant c){
			c.acmodifier-=acbonus;
		}
	}

	private DefensiveAttack(){
		super("Defensive attack","D",CombatExpertise.SINGLETON,0);
	}

	@Override
	boolean validatetarget(Combatant target){
		return true;
	}

	@Override
	int getsavebonus(Combatant targetCombatant){
		return 0;
	}

	@Override
	int getattackerbonus(Combatant combatant){
		return 0;
	}

	@Override
	public float calculatesavechance(int dc,int bonus){
		return 0;
	}

	@Override
	float getmisschance(Combatant c,Combatant target,BattleState s,int bonus){
		var misschance=super.getmisschance(c,target,s,bonus)-amount(c,4)/20f;
		return Math.max(.05f,misschance);
	}

	@Override
	ChanceNode miss(Combatant combatant,Combatant target,BattleState battleState,
			float chance){
		battleState=battleState.clone();
		combatant=battleState.clone(combatant);
		combatant.addcondition(new DefensiveStance(combatant.ap+.1f,combatant,
				defensiveamount(combatant)));
		return new ChanceNode(battleState,chance,"Defensive attack misses...",
				Javelin.Delay.WAIT);
	}

	int defensiveamount(Combatant c){
		return amount(c,Skill.ACROBATICS.getranks(c)>=3?+3:+2);
	}

	@Override
	ChanceNode hit(Combatant combatant,Combatant target,BattleState battleState,
			float chance){
		battleState=battleState.clone();
		combatant=battleState.clone(combatant);
		target=battleState.clone(target);
		combatant.addcondition(new DefensiveStance(combatant.ap+.1f,combatant,
				defensiveamount(combatant)));
		Attack attack=combatant.source.melee.get(0).get(0);
		target.damage(attack.getaveragedamage(),attack.energy?target.source.energyresistance:target.source.dr,
				battleState);
		return new ChanceNode(battleState,chance,
				"Defensive attack hits!\n"+target+" is "+target.getstatus()+".",
				Javelin.Delay.BLOCK);
	}

	static public int amount(final Combatant combatant,int fallback){
		final int acbonus=Math.min(combatant.source.getbab(),5);
		return acbonus==0?fallback:acbonus;
	}
}
