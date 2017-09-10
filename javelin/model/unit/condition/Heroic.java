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
		raiseboth(m, +2);
		m.fort += 2;
		m.ref += 2;
		m.addwill(2);
	}

	public static void raiseboth(final Monster m, int bonus) {
		raiseattacks(m.melee, bonus);
		raiseattacks(m.ranged, bonus);
	}

	public static void raiseattacks(ArrayList<AttackSequence> melee,
			int bonus) {
		for (final AttackSequence sequence : melee) {
			for (final Attack a : sequence) {
				a.bonus += bonus;
			}
		}
	}

	@Override
	public void end(final Combatant c) {
		final Monster m = c.source;
		c.source = m.clone();
		raiseboth(m, -2);
		m.fort -= 2;
		m.ref -= 2;
		m.addwill(-2);
	}
}
