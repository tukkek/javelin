package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class CatsGrace extends TotemsSpell {
	public class Graceful extends Condition {
		public Graceful(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "graceful",
					casterlevelp);
		}

		@Override
		public void start(Combatant c) {
			c.source.changedexteritymodifier(+2);
		}

		@Override
		public void end(Combatant c) {
			c.source.changedexteritymodifier(-2);
		}
	}

	public CatsGrace() {
		super("Cat's grace", Realm.WATER);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Graceful(target, casterlevel));
		return target + "'s dexterity is now "
				+ Monster.getsignedbonus(target.source.dexterity) + "!";
	}
}
