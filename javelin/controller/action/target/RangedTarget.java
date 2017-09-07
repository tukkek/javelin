package javelin.controller.action.target;

import javelin.controller.action.Action;
import javelin.controller.action.Fire;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.attack.AbstractAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.battle.BattleMouse;
import javelin.view.screen.BattleScreen;

/**
 * Gives a human-usable version of the {@link RangedAttack} {@link AiAction}.
 * This is to be used internally for convenience. Tupical ranged attacks are
 * handled by {@link Fire} (through {@link BattleMouse} if using the mouse).
 * 
 * @author alex
 */
public class RangedTarget extends Fire {
	AbstractAttack attacktype = RangedAttack.SINGLETON;
	Attack a;
	float ap;

	public RangedTarget(Attack a, float ap, char confirmkey) {
		super("Manual targetting", "", confirmkey);
		this.a = a;
		this.ap = ap;
	}

	@Override
	protected void attack(Combatant active, Combatant target, BattleState s) {
		BattleState state = Fight.state;
		active = state.clone(active);
		target = state.clone(target);
		Action.outcome(attacktype.attack(state, active, target, a, 0, 0, ap));
		BattleScreen.active.update();

	}

	@Override
	protected void checkhero(Combatant hero) {
		// assumes you've already given a valid Attack
	}

	@Override
	protected int calculatehitdc(Combatant active, Combatant target,
			BattleState s) {
		return calculatehiddc(active, target, a, RangedAttack.SINGLETON, s);
	}
}