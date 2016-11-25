package javelin.model.spell.conjuration.healing;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.unit.Combatant;

/**
 * Also features "restoration", implicitly. See the d20 SRD for more info.
 */
public class Ressurect extends Spell {
	/** Constructor. */
	public Ressurect() {
		super("Ressurection", 7,
				ChallengeRatingCalculator.ratespelllikeability(7) + RaiseDead.RESTORATIONCR,
				Realm.GOOD);
		components = 10000;
		isscroll = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.hp = target.maxhp;
		return null;
	}
}
