package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Ray;
import javelin.model.unit.condition.Dominated;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * Based on the spell Dominate Monster but trades the duration (1 day/level) to
 * a single battle and to maintain spell-level balance cuts out all the costs of
 * redirecting and commanding the enchanted target.
 *
 * It's not really a ray but we're abusing the existing logic here because it's
 * a lot easier.
 */
public class DominateMonster extends Ray {
	/** Constructor. */
	public DominateMonster() {
		super("Dominate monster", 9,
				ChallengeCalculator.ratespelllikeability(9), Realm.EVIL);
		automatichit = true;
		apcost = 1;
		castinbattle = true;
		isscroll = true;
		apcost = 1;
	}

	@Override
	public String cast(Combatant caster, Combatant target, boolean saved,
			BattleState s, ChanceNode cn) {
		cn.overlay = new AiOverlay(target.getlocation());
		if (saved) {
			return target + " resists!";
		}
		Dominated.switchteams(target, s);
		target.addcondition(
				new Dominated(Float.MAX_VALUE, target, casterlevel));
		return "Dominated " + target + "!";
	}

	@Override
	public int save(final Combatant caster, final Combatant target) {
		return calculatesavedc(target.source.will(), caster);
	}

}
