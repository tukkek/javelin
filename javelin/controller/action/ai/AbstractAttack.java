package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.ai.ChanceNode;
import javelin.controller.walker.Walker;
import javelin.model.condition.Prone;
import javelin.model.feat.PointBlankShot;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import tyrant.mikera.tyrant.Game.Delay;

public abstract class AbstractAttack extends Action implements AiAction {

	public AbstractAttack(final String name) {
		super(name);
	}

	protected abstract boolean isMelee();

	public List<ChanceNode> attack(final BattleState gameState,
			final Combatant current, final Combatant target,
			CurrentAttack attacks, int bonus) {
		final boolean pointblank = attacks == current.currentranged
				&& current.source.hasfeat(PointBlankShot.SINGLETON)
				&& Walker.distance(current, target) <= 6;
		if (pointblank) {
			bonus += 1;
		}
		if (target.hascondition(Prone.class)) {
			bonus += 2;
		}
		return attack(gameState, current, target, attacks.getnext(), bonus,
				pointblank ? 1 : 0, AbstractAttack.calculateattackap(
						getattacks(current).get(attacks.sequenceindex)));
	}

	public List<ChanceNode> attack(final BattleState gameState,
			Combatant current, final Combatant target, final Attack attack,
			final int bonus, int damagebonus, final float ap) {
		current = gameState.clone(current);
		current.ap += ap;
		final ArrayList<ChanceNode> nodes = new ArrayList<ChanceNode>();
		for (final DamageChance dc : dealattack(gameState, current, target,
				bonus, attack)) {
			if (dc.damage > 0) {
				dc.damage += damagebonus;
			}
			if (dc.damage < 0) {
				dc.damage = 0;
			}
			nodes.add(createnode(dc, target, gameState, current, attack, ap));
		}
		return nodes;
	}

	/**
	 * Always a full attack (1AP) but divided among the {@link AttackSequence}.
	 * This would penalize creatures with only one attack so max AP cost is .5
	 * per attack.
	 */
	static float calculateattackap(final AttackSequence attacks) {
		final int nattacks = attacks.size();
		if (nattacks == 1) {
			return .5f;
		}
		if (nattacks == 2) {
			/**
			 * if we let ap=.5 in this case it means that a combatant with a
			 * 2-attack sequence is identical to one with 1 attack
			 */
			return .4f;
		}
		return 1f / nattacks;
	}

	List<DamageChance> dealattack(final BattleState gameState,
			final Combatant current, final Combatant target, final int bonus,
			final Attack a) {
		final List<DamageChance> chances = new ArrayList<DamageChance>();
		final float threatchance;
		threatchance = (21 - a.threat) / 20f;
		final float misschance = misschance(gameState, current, target, bonus,
				a, getpenalty(current, target, gameState));
		final float hitchance = 1 - misschance;
		final float confirmchance = threatchance * hitchance;
		chances.add(new DamageChance(misschance, 0, false));
		AbstractAttack.hit(a, chances, hitchance - confirmchance, 1);
		AbstractAttack.hit(a, chances, confirmchance, a.multiplier);
		if (Javelin.DEBUG) {
			AbstractAttack.validate(chances);
		}
		return chances;
	}

	static public float misschance(final BattleState gameState,
			final Combatant current, final Combatant target, final int bonus,
			final Attack a, final int penalty) {
		return AbstractAttack.bind((target.ac() + penalty
				+ AbstractAttack.waterpenalty(gameState, current) - a.bonus
				- bonus - AbstractAttack.waterpenalty(gameState, target))
				/ 20f);
	}

	static public void hit(final Attack a, final List<DamageChance> chances,
			final float hitchance, final int multiplier) {
		for (final Entry<Integer, Float> roll : Action
				.distributeRoll(a.damage[0], a.damage[1]).entrySet()) {
			int damage = (roll.getKey() + a.damage[2]) * multiplier;
			if (damage < 1) {
				damage = 1;
			}
			chances.add(new DamageChance(hitchance * roll.getValue(), damage,
					multiplier != 1));
		}
	}

	static public void validate(final List<DamageChance> thisattack) {
		/* validate */
		float sum = 0;
		for (final DamageChance verify : thisattack) {
			sum += verify.chance;
		}
		if (sum > 1.001 || sum < 0.999) {
			throw new RuntimeException("Attack sum not whole: " + sum);
		}
	}

	public static int waterpenalty(final BattleState gameState,
			final Combatant current) {
		return current.source.swim()
				&& gameState.map[current.location[0]][current.location[1]].flooded
						? 2 : 0;
	}

	/**
	 * @param target
	 *            Target of the attack
	 * @return Positive integer describing a penalty.
	 */
	int getpenalty(final Combatant attacker, final Combatant target,
			final BattleState s) {
		int penalty = target.surprise();
		if (s.isflanked(target, attacker)) {
			penalty -= 2;
		}
		return penalty;
	}

	ChanceNode createnode(final DamageChance dc, Combatant target,
			final BattleState gameState, Combatant attacker,
			final Attack attack, float ap) {
		String chance =
				" (" + Javelin.translatetochance(target.ac() - attack.bonus)
						+ " to hit)...";
		if (dc.damage == 0) {
			return new ChanceNode(gameState, dc.chance,
					attacker + " misses " + target + chance, Delay.WAIT);
		}
		final BattleState attackstate = gameState.clone();
		attacker = attackstate.clone(attacker);
		target = attackstate.clone(target);
		StringBuilder messageAdd = new StringBuilder().append(attacker)
				.append(" attacks ").append(target).append(" with ")
				.append(attack.name).append(chance).append("\n");
		if (dc.critical) {
			messageAdd.append("Critical hit!\n");
		}
		if (dc.damage == 0) {
			messageAdd.append("Damage absorbed!\n");
		} else {
			target.damage(dc.damage, attackstate, target.source.dr);
			if (target.source.customName == null) {
				messageAdd.append("The ").append(target.source.name);
			} else {
				messageAdd.append(target.source.customName);
			}
			messageAdd.append(" is ").append(target.getStatus());
			if (target.hp <= 0 && cleave()) {
				attacker.cleave(ap);
			}
		}
		return new ChanceNode(attackstate, dc.chance, messageAdd.toString(),
				Delay.BLOCK);
	}

	public abstract boolean cleave();

	abstract List<AttackSequence> getattacks(Combatant active);

	List<Integer> getcurrentattack(final Combatant active) {
		final List<AttackSequence> attacktype = getattacks(active);
		if (attacktype.isEmpty()) {
			// TODO return Collections.EMPTY_LIST ?
			return new ArrayList<Integer>(0);
		}
		final CurrentAttack current = active.getcurrentattack(attacktype);
		if (current.continueattack()) {
			final ArrayList<Integer> attacks = new ArrayList<Integer>(1);
			attacks.add(current.sequenceindex);
			return attacks;
		}
		final int nattacks = attacktype.size();
		final ArrayList<Integer> attacks = new ArrayList<Integer>(nattacks);
		for (int i = 0; i < nattacks; i++) {
			attacks.add(i);
		}
		return attacks;
	}
}