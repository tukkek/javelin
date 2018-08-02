package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class Bane extends Bless {
	public class Baned extends Blessed {
		public Baned(Combatant c) {
			super(c);
			this.description = "baned";
			this.bonus = -1;
			effect = Effect.NEGATIVE;
		}
	}

	public Bane() {
		name = "Bane";
		realm = Realm.EVIL;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		String result = "";
		for (Combatant c : s.getcombatants()) {
			if (!c.isally(caster, s)
					&& getsavetarget(c.source.getwill(), caster) > 10) {
				c.addcondition(new Baned(c));
				result += c.toString() + ", ";
			}
		}
		return result.isEmpty() ? "No creatures were affected..."
				: "Baned: " + result.substring(0, result.length() - 2) + ".";
	}
}
