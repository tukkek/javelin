package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.CastSpell;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Base class for {@link MeleeAttack} and {@link RangedAttack}.
 * 
 * @author alex
 */
public abstract class AbstractAttack extends Action implements AiAction {

	class AttackNode extends ChanceNode {
		public AttackNode(Node n, float chance, String action, Delay delay,
				Combatant target) {
			super(n, chance, action, delay);
			overlay = new AiOverlay(target.location[0], target.location[1]);
		}
	}

	public AbstractAttack(final String name) {
		super(name);
	}

	protected abstract boolean isMelee();

	public List<ChanceNode> attack(final BattleState gameState,
			final Combatant current, final Combatant target,
			CurrentAttack attacks, int bonus) {
		return attack(gameState, current, target, attacks.getnext(), bonus,
				getdamagebonus(current, target),
				AbstractAttack.calculateattackap(
						getattacks(current).get(attacks.sequenceindex)));
	}

	int getdamagebonus(Combatant attacker, Combatant target) {
		return 0;
	}

	public List<ChanceNode> attack(final BattleState gameState,
			Combatant current, final Combatant target, final Attack attack,
			int bonus, int damagebonus, final float ap) {
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
			final Combatant current, final Combatant target, int bonus,
			final Attack a) {
		bonus += a.bonus;
		if (a.touch) {
			bonus += target.source.armor;
		}
		final List<DamageChance> chances = new ArrayList<DamageChance>();
		final float threatchance = (21 - a.threat) / 20f;
		final float misschance = misschance(gameState, current, target, bonus);
		final float hitchance = 1 - misschance;
		final float confirmchance = target.source.immunitytocritical ? 0
				: threatchance * hitchance;
		final float savechance = a.effect == null ? 0
				: CastSpell.savechance(current, target, a.effect);
		final float nosavechance = 1 - savechance;
		chances.add(new DamageChance(misschance, 0, false, null));
		AbstractAttack.hit(a, chances, (hitchance - confirmchance) * savechance,
				1, target, true);
		AbstractAttack.hit(a, chances,
				(hitchance - confirmchance) * nosavechance, 1, target, false);
		AbstractAttack.hit(a, chances, confirmchance * savechance, a.multiplier,
				target, true);
		AbstractAttack.hit(a, chances, confirmchance * nosavechance,
				a.multiplier, target, false);
		if (Javelin.DEBUG) {
			AbstractAttack.validate(chances);
		}
		return chances;
	}

	/**
	 * @param attackbonus
	 *            Bonus of the given any extraordinary bonuses (such as +2 from
	 *            charge). Most common chances are calculated here or by the
	 *            concrete class.
	 * @return A bound % chance of an attack completely missing it's target.
	 * @see #bind(float)
	 */
	public float misschance(final BattleState gameState,
			final Combatant current, final Combatant target,
			final int attackbonus) {
		final int penalty = getpenalty(current, target, gameState);
		final float misschance = (target.ac() + penalty - attackbonus) / 20f;
		return AbstractAttack
				.bind(addchances(misschance, target.source.misschance));
	}

	/**
	 * @return the chance of at least 1 out of 2 independent events happening,
	 *         given two percentage odds (1 = 100%).
	 */
	static public float addchances(float a, float b) {
		return a + b - a * b;
	}

	static void hit(final Attack a, final List<DamageChance> chances,
			final float hitchance, final int multiplier, Combatant target,
			boolean save) {
		if (hitchance == 0) {
			return;
		}
		for (final Entry<Integer, Float> roll : Action
				.distributeRoll(a.damage[0], a.damage[1]).entrySet()) {
			int damage = (roll.getKey() + a.damage[2]) * multiplier;
			if (damage < 1) {
				damage = 1;
			}
			chances.add(new DamageChance(hitchance * roll.getValue(), damage,
					multiplier != 1, a.effect == null ? null : save));
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

	/**
	 * @param current
	 *            Checks if swimmer.
	 * @return The penalty for attacking while standing on water (same as the
	 *         bonus for being attacked while staning on water).
	 */
	static int waterpenalty(final BattleState gameState,
			final Combatant current) {
		return current.source.swim() > 0
				&& gameState.map[current.location[0]][current.location[1]].flooded
						? 2 : 0;
	}

	/**
	 * @param target
	 *            Target of the attack
	 * @return Positive integer describing a penalty.
	 */
	public int getpenalty(final Combatant attacker, final Combatant target,
			final BattleState s) {
		return AbstractAttack.waterpenalty(s, attacker)
				- AbstractAttack.waterpenalty(s, target) + target.surprise()
				+ (target.burrowed ? 4 : 0);
	}

	ChanceNode createnode(final DamageChance dc, Combatant target,
			final BattleState gameState, Combatant attacker,
			final Attack attack, float ap) {
		String chance = " (" + getchance(attacker, target, attack, gameState)
				+ " to hit)...";
		if (dc.damage == 0) {
			return new AttackNode(gameState, dc.chance,
					attacker + " misses " + target + chance, Delay.WAIT,
					target);
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
			target.damage(dc.damage, attackstate, attack.energy
					? target.source.energyresistance : target.source.dr);
			if (target.source.customName == null) {
				messageAdd.append("The ").append(target.source.name);
			} else {
				messageAdd.append(target.source.customName);
			}
			messageAdd.append(" is ").append(target.getstatus()).append(".");
			posthit(dc, target, attacker, attack, ap, attackstate, messageAdd);
		}
		return new AttackNode(attackstate, dc.chance, messageAdd.toString(),
				Delay.BLOCK, target);
	}

	void posthit(final DamageChance dc, Combatant target, Combatant attacker,
			final Attack attack, float ap, final BattleState attackstate,
			StringBuilder messageAdd) {
		if (target.hp > 0) {
			if (dc.save != null) {
				target.source = target.source.clone();
				attacker.source = attacker.source.clone();
				messageAdd.append("\n" + attack.effect.cast(attacker, target,
						attackstate, dc.save));
			}
		} else if (cleave()) {
			attacker.cleave(ap);
		}
	}

	public abstract boolean cleave();

	abstract List<AttackSequence> getattacks(Combatant active);

	/**
	 * @return An ongoing attack or all the possible {@link AttackSequence}s
	 *         that can be initiated.
	 */
	List<Integer> getcurrentattack(final Combatant active) {
		final List<AttackSequence> attacktype = getattacks(active);
		if (attacktype.isEmpty()) {
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

	@Override
	public boolean perform(Combatant active) {
		return false;
	}

	public String getchance(Combatant current, Combatant target, Attack attack,
			BattleState s) {
		return Javelin.translatetochance(
				Math.round(20 * misschance(s, current, target, attack.bonus)));
	}
}