package javelin.controller.action.target;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.Movement;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.battle.BattleMouse;

/**
 * Human-friendly version of the {@link MeleeAttack} {@link AiAction}, to be
 * used internally. Most mêlee attacks are handled either by {@link Movement} or
 * {@link BattleMouse}.
 * 
 * @author alex
 */
public class MeleeTarget extends RangedTarget {
	public MeleeTarget(Attack a, float ap, char confirmkey) {
		super(a, ap, confirmkey);
		attacktype = MeleeAttack.SINGLETON;
	}

	@Override
	protected int calculatehitdc(Combatant active, Combatant target,
			BattleState s) {
		return calculatehiddc(active, target, a, MeleeAttack.SINGLETON, s);
	}

	@Override
	protected boolean checkengaged(BattleState state, Combatant c) {
		return false;
	}

	@Override
	protected void filtertargets(Combatant active, List<Combatant> targets,
			BattleState s) {
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			if (!active.isadjacent(target) || active.isally(target, s)) {
				targets.remove(target);
			}
		}
	}

	@Override
	public int prioritize(Combatant c, BattleState state, Combatant target) {
		return target.getnumericstatus();
	}
}