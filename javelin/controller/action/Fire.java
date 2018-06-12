package javelin.controller.action;

import java.util.List;

import javelin.controller.action.ai.attack.AbstractAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.action.target.Target;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * Ranged attack.
 * 
 * TODO make all subclasses children of {@link Target} instead.
 * 
 * @see RangedAttack
 * @author alex
 */
public class Fire extends Target {
	/** Unique instace of {@link Fire}. */
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
			BattleState battleState) {
		BattleState state = Fight.state;
		combatant = state.clone(combatant);
		targetCombatant = state.clone(targetCombatant);
		Action.outcome(RangedAttack.SINGLETON.attack(state, combatant,
				targetCombatant, combatant.chooseattack(combatant.source.ranged,
						targetCombatant),
				0));
	}

	@Override
	protected int calculatehitdc(Combatant active, final Combatant target,
			BattleState s) {
		AbstractAttack attacktype = RangedAttack.SINGLETON;
		Attack a = predictattack(active.currentranged, active.source.ranged);
		return calculatehiddc(active, target, a, attacktype, s);
	}

	public static int calculatehiddc(Combatant active, final Combatant target,
			Attack a, AbstractAttack attacktype, BattleState s) {
		final float dc = 20 * attacktype.misschance(s, active, target, a.bonus);
		return Math.round(dc);
	}

	Attack predictattack(CurrentAttack hint, List<AttackSequence> fallback) {
		AttackSequence currentranged = hint.sequenceindex == -1 ? null
				: fallback.get(hint.sequenceindex);
		Attack a = currentranged == null ? null : hint.peek();
		if (a == null) {
			a = fallback.get(0).get(0);
		}
		return a;
	}

	@Override
	protected void checkhero(Combatant hero) {
		hero.checkattacktype(false);
	}
}
