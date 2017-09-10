package javelin.model.unit.abilities.spell.totem;

import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class EaglesSplendor extends TotemsSpell {
	public class Splendid extends Condition {
		public Splendid(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "splendid",
					casterlevelp);
		}

		@Override
		public void start(Combatant c) {
			c.source.charisma += 4;
		}

		@Override
		public void end(Combatant c) {
			c.source.charisma -= 4;
		}
	}

	public EaglesSplendor() {
		super("Eagle's splendor", Realm.AIR);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		target.addcondition(new Splendid(target, casterlevel));
		return target + "'s charisma is now "
				+ Monster.getsignedbonus(target.source.charisma);
	}

}
