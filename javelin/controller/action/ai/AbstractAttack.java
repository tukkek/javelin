package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.ai.ChanceNode;
import javelin.controller.walker.Walker;
import javelin.model.feat.PointBlankShot;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import tyrant.mikera.tyrant.Game.Delay;

public abstract class AbstractAttack extends Action {

	private class DamageChance {
		final float chance;
		int damage;
		private boolean critical;

		public DamageChance(final float chance, final int damage,
				boolean criticalp) {
			super();
			this.chance = chance;
			this.damage = damage;
			critical = criticalp;
		}
	}

	public AbstractAttack(final String name) {
		super(name);
	}

	protected abstract boolean isMelee();

	public List<ChanceNode> attack(final BattleState gameState,
			final Combatant current, final Combatant target,
			CurrentAttack attacks, int bonus) {
		final boolean pointblank = attacks == current.currentranged
				&& current.source.hasfeat(PointBlankShot.SINGLETON) > 0
				&& Walker.distance(current, target) <= 6;
		if (pointblank) {
			bonus += 1;
		}
		return attack(gameState, current, target, attacks, bonus,
				pointblank ? 1 : 0,
				calculateattackap(getattacks(current)
						.get(attacks.sequenceindex)));
	}

	public List<ChanceNode> attack(final BattleState gameState,
			final Combatant current, final Combatant target,
			CurrentAttack attacks, int bonus, int damagebonus, float ap) {
		return attack(gameState, current, target, attacks.getnext(), bonus,
				damagebonus, ap);
	}

	public List<ChanceNode> attack(final BattleState gameState,
			final Combatant current, final Combatant target,
			final Attack attack, final int bonus, int damagebonus,
			final float ap) {
		final Combatant sameCombatant = gameState.translatecombatant(current);
		sameCombatant.ap += ap;
		final ArrayList<ChanceNode> nodes = new ArrayList<ChanceNode>();
		for (final DamageChance dc : dealattack(gameState, current, target,
				bonus, attack)) {
			if (dc.damage > 0) {
				dc.damage += damagebonus;
			}
			if (dc.damage < 0) {
				dc.damage = 0;
			}
			nodes.add(createchancenode(dc, target, gameState, current, attack));
		}
		return nodes;
	}

	/**
	 * Always a full attack (1AP) but divided among the {@link AttackSequence}.
	 * This would penalize creatures with only one attack so max AP cost is .5
	 * per attack.
	 */
	public float calculateattackap(AttackSequence attacks) {
		int nattacks = attacks.size();
		switch (nattacks) {
		case 1:
			return .5f;
		case 2:
			/**
			 * if we let ap=.5 in this case it means that a combatant with a
			 * 2-attack sequence is identical to one with 1 attack
			 */
			return .4f;
		default:
			return 1f / nattacks;
		}
	}

	public List<DamageChance> dealattack(final BattleState gameState,
			final Combatant current, final Combatant target, final int bonus,
			final Attack a) {
		final List<DamageChance> chances = new ArrayList<AbstractAttack.DamageChance>();
		final float threatchance = (21 - a.threat) / 20f;
		final float misschance = bind((target.ac() - a.bonus
				+ getpenalty(current, target, gameState) - (bonus
				- waterpenalty(gameState, current) + waterpenalty(gameState,
				target))) / 20f);
		final float hitchance = 1 - misschance;
		final float confirmchance = threatchance * hitchance;
		chances.add(new DamageChance(misschance, 0, false));
		hit(a, chances, hitchance - confirmchance, 1);
		hit(a, chances, confirmchance, a.multiplier);
		if (Javelin.DEBUG) {
			validate(chances);
		}
		return chances;
	}

	public void hit(final Attack a, final List<DamageChance> chances,
			final float hitchance, final int multiplier) {
		for (final Entry<Integer, Float> roll : distributeRoll(a.damage[0],
				a.damage[1]).entrySet()) {
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

	static public float bind(float misschance) {
		if (misschance > .95f) {
			return .95f;
		}
		if (misschance < 0.05f) {
			return .05f;
		}
		return misschance;
	}

	public static int waterpenalty(final BattleState gameState,
			final Combatant current) {
		return current.source.swim()
				&& gameState.map[current.location[0]][current.location[1]].flooded ? 2
				: 0;
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

	private ChanceNode createchancenode(DamageChance dc, Combatant target,
			final BattleState gameState, Combatant attacker, final Attack attack) {
		final BattleState attackstate = gameState.clone();
		attacker = attackstate.translatecombatant(attacker);
		target = attackstate.translatecombatant(target);
		final String message;
		Delay delay;
		if (dc.damage == 0) {
			delay = Delay.WAIT;
			message = attacker + " misses " + target + "...";
		} else {
			delay = Delay.BLOCK;
			String messageAdd = "\n";
			if (dc.critical) {
				messageAdd += "Critical hit!\n";
			}
			// TODO apply DR
			if (dc.damage == 0) {
				messageAdd += "Damage absorbed!";
			} else {
				target.damage(dc.damage, attackstate, target.source.dr);
				messageAdd += status(target, target.getStatus());
			}
			message = attacker + " attacks " + target + " with "
					+ attack.toString().toLowerCase() + messageAdd;
		}
		return new ChanceNode(attackstate, dc.chance, message, delay);
	}

	private String status(final Combatant target, final String status) {
		return (target.source.customName == null ? "The " + target.source.name
				: target.source.customName) + " is " + status;
	}

	abstract List<AttackSequence> getattacks(Combatant active);

	List<Integer> getcurrentattack(Combatant active) {
		final List<AttackSequence> attacktype = getattacks(active);
		final ArrayList<Integer> attacks = new ArrayList<Integer>();
		if (attacktype.isEmpty()) {
			return attacks;
		}
		final CurrentAttack current = active.getcurrentattack(attacktype);
		if (current.continueattack()) {
			attacks.add(current.sequenceindex);
		} else {
			for (int i = 0; i < attacktype.size(); i++) {
				attacks.add(i);
			}
		}
		return attacks;
	}
}