package javelin.controller.action.ai.attack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.CastSpell;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.skill.Bluff;

/**
 * Base class for {@link MeleeAttack} and {@link RangedAttack}.
 *
 * @author alex
 */
public abstract class AbstractAttack extends Action implements AiAction {
	static final ConcurrentHashMap<Thread, Strike> CURRENTMANEUVER = new ConcurrentHashMap<Thread, Strike>();

	/** @see Bluff#feign(Combatant) */
	protected boolean feign = false;

	public AbstractAttack(final String name) {
		super(name);
	}

	protected abstract boolean isMelee();

	public List<ChanceNode> attack(final BattleState s, final Combatant current,
			final Combatant target, CurrentAttack attacks, int bonus) {
		final Attack a = attacks.getnext();
		final int damagebonus = getdamagebonus(current, target);
		final float ap = AbstractAttack.calculateattackap(
				getattacks(current).get(attacks.sequenceindex));
		return attack(current, target, a, bonus, damagebonus, ap, s);
	}

	protected int getdamagebonus(Combatant attacker, Combatant target) {
		return 0;
	}

	public List<ChanceNode> attack(Combatant attacker, Combatant target,
			final Attack a, int attackbonus, int damagebonus, final float ap,
			BattleState s) {
		final Strike m = getmaneuver();
		s = s.clone();
		attacker = s.clone(attacker);
		attacker.ap += ap;
		if (m != null) {
			m.preattacks(attacker, target, a, s);
		}
		final ArrayList<ChanceNode> nodes = new ArrayList<ChanceNode>();
		for (final DamageChance dc : dealattack(attacker, target, a,
				attackbonus, s)) {
			if (dc.damage > 0) {
				dc.damage += damagebonus;
			}
			if (dc.damage < 0) {
				dc.damage = 0;
			}
			nodes.add(createnode(attacker, target, a, ap, m, dc, s));
		}
		if (m != null) {
			m.postattacks(attacker, target, a, s);
		}
		return nodes;
	}

	ChanceNode createnode(Combatant attacker, Combatant target, final Attack a,
			float ap, final Strike m, final DamageChance dc, BattleState s) {
		final String tohit = " (" + getchance(attacker, target, a, s)
				+ " to hit)...";
		StringBuilder sb = new StringBuilder(attacker.toString());
		if (dc.damage == 0) {
			return miss(attacker, target, m, dc, s, sb, tohit);
		}
		s = s.clone();
		attacker = s.clone(attacker);
		target = s.clone(target);
		if (m != null) {
			m.prehit(attacker, target, a, dc, s);
		}
		final String name = m == null ? a.name : m.name.toLowerCase();
		sb.append(" attacks ").append(target).append(" with ").append(name)
				.append(tohit);
		if (dc.critical) {
			sb.append("\nCritical hit!");
		}
		if (dc.damage == 0) {
			sb.append("\nDamage absorbed!");
		} else {
			target.damage(dc.damage, s, a.energy
					? target.source.energyresistance : target.source.dr);
			if (target.source.customName == null) {
				sb.append("\nThe ").append(target.source.name);
			} else {
				sb.append("\n").append(target.source.customName);
			}
			sb.append(" is ").append(target.getstatus()).append(".");
			posthit(attacker, target, a, ap, dc, s, sb);
		}
		if (m != null) {
			m.posthit(attacker, target, a, dc, s);
		}
		final Delay delay = target.source.passive
				&& target.getnumericstatus() > Combatant.STATUSUNCONSCIOUS
						? Delay.WAIT : Delay.BLOCK;
		return new DamageNode(s, dc.chance, sb.toString(), delay, target);
	}

	/**
	 * Always a full attack (1AP) but divided among the {@link AttackSequence}.
	 * This would penalize creatures with only one attack so max AP cost is .5
	 * per attack.
	 *
	 * If a {@link #CURRENTMANEUVER} is being used, returns {@link Maneuver}
	 * instead.
	 */
	static float calculateattackap(final AttackSequence attacks) {
		final Maneuver m = getmaneuver();
		if (m != null) {
			return m.ap;
		}
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

	List<DamageChance> dealattack(final Combatant active,
			final Combatant target, final Attack a, int bonus,
			final BattleState s) {
		bonus += a.bonus;
		if (a.touch) {
			bonus += target.source.armor;
		}
		final List<DamageChance> chances = new ArrayList<DamageChance>();
		final float threatchance = (21 - a.threat) / 20f;
		final float misschance = misschance(s, active, target, bonus);
		final float hitchance = 1 - misschance;
		final float confirmchance = target.source.immunitytocritical ? 0
				: threatchance * hitchance;
		final Spell effect = target.source.passive ? null : a.geteffect();
		final float savechance = effect == null ? 1
				: CastSpell.savechance(active, target, effect);
		final float nosavechance = 1 - savechance;
		chances.add(new DamageChance(misschance, 0, false, null));
		hit(a, (hitchance - confirmchance) * savechance, 1, target, true,
				chances);
		hit(a, (hitchance - confirmchance) * nosavechance, 1, target, false,
				chances);
		hit(a, confirmchance * savechance, a.multiplier, target, true, chances);
		hit(a, confirmchance * nosavechance, a.multiplier, target, false,
				chances);
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
		final float misschance = (target.getac() + penalty - attackbonus) / 20f;
		return Action.bind(addchances(misschance, target.source.misschance));
	}

	/**
	 * @return the chance of at least 1 out of 2 independent events happening,
	 *         given two percentage odds (1 = 100%).
	 */
	static public float addchances(float a, float b) {
		return a + b - a * b;
	}

	static void hit(final Attack a, final float hitchance, final int multiplier,
			Combatant target, boolean save, final List<DamageChance> chances) {
		if (hitchance == 0) {
			return;
		}
		for (final Entry<Integer, Float> roll : Action
				.distributeroll(a.damage[0], a.damage[1]).entrySet()) {
			int damage = (roll.getKey() + a.damage[2]) * multiplier;
			if (damage < 1) {
				damage = 1;
			}
			final float chance = hitchance * roll.getValue();
			chances.add(new DamageChance(chance, damage, multiplier != 1,
					a.geteffect() == null ? null : save));
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

	DamageNode miss(Combatant attacker, Combatant target, final Strike m,
			final DamageChance dc, BattleState s, final StringBuilder sb,
			final String tohit) {
		if (feign && target.source.dexterity >= 12) {
			s = s.clone();
			target = s.clone(target);
			Bluff.feign(attacker, target);
		}
		final String name;
		final Delay wait;
		if (m == null) {
			name = target.toString();
			wait = Delay.WAIT;
		} else {
			name = m.name.toLowerCase();
			wait = Delay.BLOCK;
		}
		sb.append(" misses ").append(name).append(tohit);
		return new DamageNode(s, dc.chance, sb.toString(), wait, target);
	}

	void posthit(Combatant active, Combatant target, final Attack a, float ap,
			final DamageChance dc, final BattleState s, StringBuilder sb) {
		if (target.hp <= 0) {
			if (cleave()) {
				active.cleave(ap);
			}
		} else if (dc.save != null) {
			target.source = target.source.clone();
			active.source = active.source.clone();
			final String effect = a.geteffect().cast(active, target, dc.save, s,
					null);
			sb.append("\n").append(effect);
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

	static Strike getmaneuver() {
		return CURRENTMANEUVER.get(Thread.currentThread());
	}

	/**
	 * Sets the current {@link Maneuver} which should be taken as context during
	 * the execution of this class. Since this class needs to be thread-safe
	 * this is backed by a {@link ConcurrentHashMap} in order to properly
	 * synchronize setting and clearing this for any given thread at the right
	 * time.
	 */
	public static void setmaneuver(Strike m) {
		final Thread t = Thread.currentThread();
		if (m == null) {
			CURRENTMANEUVER.remove(t);
		} else {
			CURRENTMANEUVER.put(t, m);
		}
	}
}