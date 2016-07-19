package javelin.controller.action;

import java.util.List;

import javelin.controller.action.ai.RangedAttack;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import tyrant.mikera.engine.Thing;

/**
 * Ranged attack.
 * 
 * TODO make all subclasses children of {@link Target} instead.
 * 
 * @see RangedAttack
 * @author alex
 */
public class Fire extends Target {

	public static final Fire SINGLETON = new Fire();

	/**
	 * @param confirm
	 *            Usually the same as the action key so as to make pressing the
	 *            same key twice a "invoke action" + "confirm targeting".
	 * @see Action#Action(String, String).
	 */
	public Fire(final String name, final String key, char confirm) {
		super(name, key);
		this.confirmkey = confirm;
	}

	private Fire() {
		this("Fire or throw ranged weapon", "f", 'f');
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, final BattleMap map) {
		BattleState state = map.getState();
		combatant = state.clone(combatant);
		targetCombatant = state.clone(targetCombatant);
		Action.outcome(RangedAttack.SINGLETON.attack(state, combatant,
				targetCombatant, combatant.chooseattack(combatant.source.ranged,
						targetCombatant),
				0));
	}

	@Override
	protected int calculatehitdc(final Combatant target, Combatant active,
			BattleState state) {
		return Math.round(20 * RangedAttack.SINGLETON.misschance(state, active,
				target, predictattack(active.currentranged,
						active.source.ranged).bonus));
		// return target.ac()
		// - predictattack(active.currentranged,
		// active.source.ranged).bonus
		// - prioritize(active, state, target)
		// + RangedAttack.SINGLETON.getpenalty(active, target, state);
	}

	protected Attack predictattack(CurrentAttack hint,
			List<AttackSequence> fallback) {
		AttackSequence currentranged = hint.sequenceindex == -1 ? null
				: fallback.get(hint.sequenceindex);
		Attack a = currentranged == null ? null : hint.peek();
		if (a == null) {
			a = fallback.get(0).get(0);
		}
		return a;
	}

	@Override
	protected void checkhero(Thing hero) {
		hero.combatant.checkAttackType(false);
	}
}
