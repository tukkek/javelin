package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class BullsStrength extends TotemsSpell {
	public class Strong extends Condition {
		public Strong(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "strong", casterlevelp);
		}

		@Override
		public void start(Combatant c) {
			c.source.raisestrength(+2);
		}

		@Override
		public void end(Combatant c) {
			c.source.raisestrength(-2);
		}
	}

	public BullsStrength() {
		super("Bull's strength", Realm.FIRE);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Strong(target, casterlevel));
		return target + "'s strength is now "
				+ Monster.getsignedbonus(target.source.strength);
	}

}
