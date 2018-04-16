package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Heroic;

/**
 * http://www.d20pfsrd.com/magic/all-spells/b/bless/
 * 
 * @author alex
 */
public class Bless extends Spell {
	public class Blessed extends Condition {
		int bonus = +1;

		public Blessed(Combatant c) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "blessed", 1);
		}

		@Override
		public void start(Combatant c) {
			c.source = c.source.clone();
			Heroic.raiseallattacks(c.source, bonus, 0);
		}

		@Override
		public void end(Combatant c) {
			c.source = c.source.clone();
			Heroic.raiseallattacks(c.source, -bonus, 0);
		}
	}

	public Bless() {
		super("Bless", 1, ChallengeCalculator.ratespelllikeability(1), Realm.GOOD);
		castonallies = true;
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		s = s.clone();
		for (Combatant c : s.getteam(caster)) {
			c.addcondition(new Blessed(c));
		}
		return "All allies are blessed!";
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
