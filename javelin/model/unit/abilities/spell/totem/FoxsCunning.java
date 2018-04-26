package javelin.model.unit.abilities.spell.totem;

import javelin.controller.ai.ChanceNode;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class FoxsCunning extends TotemsSpell {
	public class Cunning extends Condition {
		/**
		 * Constructor.
		 * 
		 * @param casterlevelp
		 */
		public Cunning(Combatant c, Integer casterlevelp) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "cunning", casterlevelp);
		}

		@Override
		public void start(Combatant c) {
			c.source.intelligence += 4;
		}

		@Override
		public void end(Combatant c) {
			c.source.intelligence += 4;
		}
	}

	public FoxsCunning() {
		super("Fox's cunning", Realm.EVIL);
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final boolean saved, final BattleState s, ChanceNode cn) {
		target.addcondition(new Cunning(target, casterlevel));
		return target + "'s intelligence is now "
				+ Monster.getsignedbonus(target.source.intelligence) + "!";
	}

}
