package javelin.model.condition;

import java.util.ArrayList;

import javelin.model.spell.enchantment.compulsion.Heroism;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * @see javelin.model.spell.enchantment.compulsion.Heroism
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
		c.source = c.source.clone();
		raiseattacks(c.source.melee, +2);
		raiseattacks(c.source.ranged, +2);
		c.source.fort += 2;
		c.source.ref += 2;
		c.source.addwill(2);
	}

	public void raiseattacks(ArrayList<AttackSequence> melee, int bonus) {
		for (final AttackSequence sequence : melee) {
			for (final Attack a : sequence) {
				a.bonus += bonus;
			}
		}
	}

	@Override
	public void end(final Combatant c) {
		c.source = c.source.clone();
		raiseattacks(c.source.melee, -2);
		raiseattacks(c.source.ranged, -2);
		c.source.fort -= 2;
		c.source.ref -= 2;
		c.source.addwill(-2);
	}

}
