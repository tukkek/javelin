package javelin.model.spell.illusion;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.condition.Blinking;
import javelin.model.spell.Touch;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class Displacement extends Touch {
	protected int turns = 6;

	public Displacement() {
		this("Displacement", 3,
				ChallengeRatingCalculator.ratespelllikeability(3), Realm.MAGIC);
		iswand = true;
	}

	public Displacement(String name, int levelp, float incrementcost,
			Realm realmp) {
		super(name, levelp, incrementcost, realmp);
		castinbattle = true;
		castonallies = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		target.addcondition(
				new Blinking(caster.ap + turns, caster, casterlevel));
		return target + " is blinking!";
	}
}
