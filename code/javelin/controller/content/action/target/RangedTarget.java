package javelin.controller.content.action.target;

import javelin.controller.content.action.Action;
import javelin.controller.content.action.Fire;
import javelin.controller.content.action.ai.AiAction;
import javelin.controller.content.action.ai.attack.AbstractAttack;
import javelin.controller.content.action.ai.attack.AttackResolver;
import javelin.controller.content.action.ai.attack.RangedAttack;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.view.mappanel.battle.BattleMouse;

/**
 * Gives a human-usable version of the {@link RangedAttack} {@link AiAction}.
 * This is to be used internally for convenience. Tupical ranged attacks are
 * handled by {@link Fire} (through {@link BattleMouse} if using the mouse).
 *
 * Performs a single {@link Attack}, not an {@link AttackSequence}. See
 * {@link AttackResolver}.
 *
 * @author alex
 */
public class RangedTarget extends Fire{
	AbstractAttack action=RangedAttack.INSTANCE;
	AttackSequence sequence;
	Attack a;

	public RangedTarget(Attack a,AttackSequence sequence,char confirmkey,
			AbstractAttack action){
		super("Manual targetting","",confirmkey);
		this.a=a;
		this.sequence=sequence;
		this.action=action;
	}

	@Override
	protected void attack(Combatant active,Combatant target,BattleState s){
		var resolver=new AttackResolver(action,active,target,a,Fight.state);
		Action.outcome(resolver.attack(active,target,s));
	}

	@Override
	protected void checkhero(Combatant hero){
		// assumes you've already given a valid Attack
	}

	@Override
	protected int predictchance(Combatant c,Combatant target,BattleState s){
		return calculatehiddc(c,target,a,sequence,action,s);
	}
}