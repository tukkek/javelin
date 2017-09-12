package javelin.model.unit.condition;

import java.util.ArrayList;

import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;

/**
 * @see javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism
 * @see Heroism
 * @author alex
 */
public class Heroic extends Condition {
	public Heroic(final Combatant c, Integer casterlevelp, Integer longtermp) {
		super(Float.MAX_VALUE, c, Effect.POSITIVE, "heroic", casterlevelp,
				longtermp);
	}

	public Heroic(Combatant c, Integer casterlevelp) {
		this(c, casterlevelp, 1);
	}

	@Override
	public void start(final Combatant c) {
		final Monster m = c.source;
		c.source = m.clone();
		raiseallattacks(m, +2, +0);
		raisesaves(m, +2);
	}

	public static void raisesaves(final Monster m, int amount) {
		m.fort += amount;
		m.ref += amount;
		m.addwill(amount);
	}

	public static void raiseallattacks(final Monster m, int attackbonus,
			int damagebonus) {
		raiseattacks(m.melee, attackbonus, damagebonus);
		raiseattacks(m.ranged, attackbonus, damagebonus);
	}

	public static void raiseattacks(ArrayList<AttackSequence> melee,
			int attackbonus, int damagebonus) {
		for (final AttackSequence sequence : melee) {
			for (final Attack a : sequence) {
				a.bonus += attackbonus;
				a.damage[2] += damagebonus;
			}
		}
	}

	@Override
	public void end(final Combatant c) {
		final Monster m = c.source;
		c.source = m.clone();
		raiseallattacks(m, -2, -0);
		m.fort -= 2;
		m.ref -= 2;
		m.addwill(-2);
	}
}
