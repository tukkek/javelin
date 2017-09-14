package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.challenge.CrCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

public class Rage extends Spell {
	public class Raging extends Condition {
		public Raging(float expireatp, Combatant c, Integer casterlevel) {
			super(expireatp, c, Effect.POSITIVE, "raging", casterlevel);
		}

		@Override
		public void start(Combatant c) {
			Monster m = c.source.clone();
			c.source = m;
			m.changestrengthmodifier(+1);
			m.changeconstitutionmodifier(c, +1);
			m.addwill(+1);
			c.acmodifier -= 2;
		}

		@Override
		public void end(Combatant c) {
			Monster m = c.source.clone();
			c.source = m;
			m.changestrengthmodifier(-1);
			m.changeconstitutionmodifier(c, -1);
			m.addwill(-1);
			c.acmodifier += 2;
		}
	}

	public Rage() {
		super("Rage", 3, CrCalculator.ratespelllikeability(3),
				Realm.FIRE);
		castinbattle = true;
		castonallies = true;
		ispotion = true;
	}

	public Rage(String name, int levelp, float incrementcost, Realm realmp) {
		super(name, levelp, incrementcost, realmp);
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		float expiresat = caster.ap + getduration(target);
		target.addcondition(new Raging(expiresat, target, casterlevel));
		return target + " is raging!";
	}

	float getduration(Combatant target) {
		return Math.max(1, 4 + Monster.getbonus(target.source.constitution));
	}
}
