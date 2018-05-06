package javelin.model.unit.abilities.spell.totem;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Common implementation of this type of spell.
 *
 * @author alex
 */
public abstract class TotemsSpell extends Touch {

	public TotemsSpell(String name, Realm realmp) {
		super(name, 2, ChallengeCalculator.ratespelllikeability(2), realmp);
		castonallies = true;
		castinbattle = true;
		ispotion = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		cn.overlay = new AiOverlay(target.getlocation());
		return super.cast(caster, target, saved, s, cn);
	}
}